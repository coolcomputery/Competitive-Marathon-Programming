import java.io.*;
import java.util.*;
public class SnakeCharmer {
    private static final PrintStream P=System.err;
    private static final char[] names = {'D','R','U','L'};
    private static final int[] dr={1,0,-1,0}, dc={0,1,0,-1};
    private int[] snake;
    private int N;
    private void add(StringBuilder s, int d, int n) {
        for (int i=0; i<n; i++)
            s.append(d);
    }
    private void add(StringBuilder s, int d) {
        s.append(d);
    }
    private StringBuilder inv(StringBuilder mvs) {
        StringBuilder out=new StringBuilder();
        for (int i=mvs.length()-1; i>-1; i--)
            out.append((mvs.charAt(i)-'0'+2)%4);
        return out;
    }
    private int scr(CharSequence mvs) {
        int[][] grid=new int[N][N];
        for (int i=0, r=N/2, c=N/2; true; i++) {
            if (r<0 || r>=N || c<0 || c>=N
                    || grid[r][c]!=0) {
                return -1;
            }
            grid[r][c]=snake[mvs.length()-i];
            if (i==mvs.length())
                break;
            int d=mvs.charAt(i)-'0';
            r+=dr[d];
            c+=dc[d];
        }
        int scr=0;
        for (int i=0, r=N/2, c=N/2; true; i++) {
            int ncnt=0;
            for (int d=0; d<4; d++) {
                int nr=r+dr[d], nc=c+dc[d];
                if (nr>-1 && nr<N && nc>-1 && nc<N
                        && grid[nr][nc]==grid[r][c])
                    ncnt++;
            }
            scr+=Math.pow(grid[r][c],ncnt+1);
            if (i==mvs.length())
                break;
            int d=mvs.charAt(i)-'0';
            r+=dr[d];
            c+=dc[d];
        }
        return scr;
    }
    private Sol best(StringBuilder mvs) {
        StringBuilder out=new StringBuilder();
        int bscr=scr(out);
        for (int i=0; i<mvs.length(); i++) {
            int scr=scr(mvs.substring(0,i+1));
            if (scr>bscr) {
                bscr=scr;
                for (int j=out.length(); j<=i; j++)
                    out.append(mvs.charAt(j));
            }
        }
        return new Sol(out,bscr);
    }
    private StringBuilder spiral(boolean[] flip) {
        StringBuilder out=new StringBuilder();
        for (int li=0; li<N/2; li++) {
            int l=2*li+2;
            if (flip[li]) {
                out.append(3);
                for (int i = 0; i < l - 1; i++)
                    out.append(2);
                for (int i = 0; i < l; i++)
                    out.append(1);
                for (int i = 0; i < l; i++)
                    out.append(0);
                for (int i = 0; i < l; i++)
                    out.append(3);
            }
            else {
                out.append(0);
                for (int i = 0; i < l - 1; i++)
                    out.append(1);
                for (int i = 0; i < l; i++)
                    out.append(2);
                for (int i = 0; i < l; i++)
                    out.append(3);
                for (int i = 0; i < l; i++)
                    out.append(0);
            }
        }
        return out;
    }
    public char[] findSolution(int N, int V, String snakeStr) {
        long END=System.currentTimeMillis()+9500;
        this.N=N;
        this.snake=new int[N*N];
        for (int i=0; i<snakeStr.length(); i++)
            snake[i]=snakeStr.charAt(i)-'0';
        Sol bspiral=new Sol(new StringBuilder());
        if (N<=19) {
            boolean[] flip=new boolean[N/2];
            flip[0]=true;
            while (System.currentTimeMillis()<END) {
                Sol s = best(spiral(flip));
                if (s.scr > bspiral.scr)
                    bspiral = s;
                int j=0;
                for (; j<flip.length && flip[j]; j++)
                    flip[j]=false;
                if (j>=flip.length)
                    break;
                flip[j]=true;
            }
            P.println("best spiral="+bspiral.scr);
        }
        Sol bbeam=new Sol(new StringBuilder());
        int w=7;
        w=Math.min(N,w);
        if (N<=9) {
            for (int sz=1000, last=bbeam.scr; System.currentTimeMillis()<END; sz*=3) {
                long st=System.currentTimeMillis();
                List<InvSol> invs=new ArrayList<>();
                invs.add(new InvSol(new StringBuilder()));
                for (int r=1; r<N*N; r++) {
                    if (System.currentTimeMillis() > END) {
                        for (InvSol inv:invs)
                            bbeam=best(bbeam,inv.realized());
                        break;
                    }
                    List<InvSol> ninvs=new ArrayList<>();
                    for (InvSol inv:invs)
                        for (int d=0; d<4; d++) {
                            if (inv.mvs.length()>0 && inv.mvs.charAt(inv.mvs.length()-1)-'0'==(d+2)%4)
                                continue;
                            StringBuilder s=new StringBuilder(inv.mvs);
                            s.append(d);
                            InvSol ni=new InvSol(s);
                            if (ni.scr>-1)
                                ninvs.add(ni);
                        }
                    ninvs.sort(new Comparator<InvSol>() {
                        @Override
                        public int compare(InvSol o1, InvSol o2) {
                            return o2.scr - o1.scr;
                        }
                    });
                    //if (r>=N)
                    for (int i=0; i<ninvs.size(); i++) {
                        /*if (ninvs.get(i).scr<bbeam.scr)
                            continue;*/
                        Sol s=ninvs.get(i).realized();
                        if (s!=null && s.scr>bbeam.scr) {
                            bbeam=s;
                        }
                    }
                    invs.clear();
                    if (ninvs.size() == 0)
                        break;
                    for (int i = 0; i < ninvs.size() && i < sz; i++)
                        invs.add(ninvs.get(i));
                /*if (r%N==0)
                    P.println(r+" "+bbeam.scr);*/
                }
                //if (bbeam.scr>last) {
                P.println("sz=" + sz + " time=" + (System.currentTimeMillis() - st) + " " + bbeam.scr);
                last=bbeam.scr;
                //}
            }
        }
        else {
            for (int sz = 40, last = bbeam.scr; System.currentTimeMillis() < END; sz += (int) (10 * Math.pow(49.0 / N, 2))) {
                long st = System.currentTimeMillis();
                List<InvSol> invs = new ArrayList<>();
                invs.add(new InvSol(new StringBuilder()));
                invs.get(0).mark = 0;
                for (int r = 0, minc = 0, maxc = N - 1; r < N - 1; r++) {
                    if (System.currentTimeMillis() > END) {
                        for (InvSol inv : invs)
                            bbeam = best(bbeam, inv.realized());
                        break;
                    }
                    if (r == N / 2) {
                        if (r % 2 == 0)
                            maxc = N / 2 - 1;
                        else
                            minc = N / 2 + 1;
                    } else if (r > N / 2) {
                        if (N / 2 % 2 == 0)
                            maxc = N - 2;
                        else
                            minc = 1;
                    }
                    List<InvSol> ninvs = new ArrayList<>();
                    for (InvSol inv : invs) {
                        //P.print(inv.mark+" ");
                        int maxi = (r % 2 == 0 ? maxc - inv.mark : inv.mark - minc);
                        for (int i = (r == N / 2 || r == N / 2 + 1 ? 0 : Math.max(0, maxi - w)); i <= maxi; i++) {
                            StringBuilder s = new StringBuilder(inv.mvs);
                            add(s, r % 2 == 0 ? 1 : 3, i);
                            add(s, 0);
                            InvSol n = new InvSol(s);
                            n.mark = inv.mark + (r % 2 == 0 ? 1 : -1) * i;
                            ninvs.add(n);
                        }
                    }
                    //P.println();
                    ninvs.sort(new Comparator<InvSol>() {
                        @Override
                        public int compare(InvSol o1, InvSol o2) {
                            return o2.scr - o1.scr;
                        }
                    });
                    if (r > N - 4)
                        for (int i = 0; i < ninvs.size(); i++) {
                            Sol s = ninvs.get(i).realized();
                            if (s.scr > bbeam.scr) {
                                bbeam = s;
                                //P.println(r+" "+i+" "+s.scr);
                            /*if (r<N-2)
                                break;*/
                            }
                        }
                /*for (InvSol i:ninvs)
                    P.print(i.scr+" ");
                P.println();*/
                    invs.clear();
                    //P.println(r+" "+minc+" "+maxc+" "+best.scr);
                    if (ninvs.size() == 0)
                        break;
                    for (int i = 0; i < ninvs.size() && i < sz; i++)
                        invs.add(ninvs.get(i));
                }
                if (bbeam.scr > last) {
                    P.println("sz=" + sz + " time=" + (System.currentTimeMillis() - st) + " " + bbeam.scr);
                    last = bbeam.scr;
                }
            }
        }
        Sol best=best(bspiral,bbeam);
        StringBuilder mvs=best.mvs;
        char[] out=new char[mvs.length()];
        for (int i=0; i<mvs.length(); i++)
            out[i]=names[mvs.charAt(i)-'0'];
        P.println("best="+best.scr+", len="+mvs.length());
        return out;
    }
    private Sol best(Sol a, Sol b) {
        return a==null?b:b==null?a:a.scr>b.scr?a:b;
    }
    private class Sol {
        StringBuilder mvs;
        int scr;
        public Sol(StringBuilder mvs) {
            this.mvs=mvs;
            scr=scr(mvs);
        }
        public Sol(StringBuilder mvs, int scr) {
            this.mvs=mvs;
            this.scr=scr;
        }
    }
    private class InvSol {
        StringBuilder mvs;
        int[][] grid;
        int scr;
        int fr, fc;
        int minr, minc, maxr, maxc;
        int mark;
        public InvSol(StringBuilder mvs) {
            minr=Integer.MAX_VALUE;
            minc=minr;
            maxr=Integer.MIN_VALUE;
            maxc=maxr;
            this.mvs=new StringBuilder(mvs);
            grid=new int[2*N][2*N];
            for (int i=0, r=N, c=N; true; i++) {
                if (r<0 || r>=2*N || c<0 || c>=2*N
                        || grid[r][c]!=0) {
                    scr=-1;
                    return;
                }
                grid[r][c]=snake[i];
                minr=Math.min(minr,r);
                maxr=Math.max(maxr,r);
                minc=Math.min(minc,c);
                maxc=Math.max(maxc,c);
                if (i==mvs.length()) {
                    fr=r;
                    fc=c;
                    break;
                }
                int d=mvs.charAt(i)-'0';
                r+=dr[d];
                c+=dc[d];
            }
            scr=0;
            for (int i=0, r=N, c=N; true; i++) {
                int ncnt=0;
                for (int d=0; d<4; d++) {
                    int nr=r+dr[d], nc=c+dc[d];
                    if (nr>-1 && nr<2*N && nc>-1 && nc<2*N
                            && grid[nr][nc]==grid[r][c])
                        ncnt++;
                }
                scr+=Math.pow(grid[r][c],ncnt+1);
                if (i==mvs.length())
                    break;
                int d=mvs.charAt(i)-'0';
                r+=dr[d];
                c+=dc[d];
            }
        }
        public Sol realized() {
            for (int or=N-minr; or<2*N-maxr; or++)
                for (int oc=N-minc; oc<2*N-maxc; oc++) {
                    Sol out=realized(or,oc);
                    if (out!=null)
                        return out;
                }
            return null;
        }
        private StringBuilder path(boolean[][] open, boolean[][] goal, int sr, int sc) {
            boolean[][] seen=new boolean[N][N];
            seen[sr][sc]=true;
            int[][] dir=new int[N][N];
            for (int i=0; i<N*N; i++)
                dir[i/N][i%N]=-1;
            List<Integer> f=new ArrayList<>();
            f.add(sr*N+sc);
            while (f.size()>0) {
                List<Integer> nf=new ArrayList<>();
                for (int v:f) {
                    int r=v/N, c=v%N;
                    for (int d=0; d<4; d++) {
                        int nr = r + dr[d], nc = c + dc[d];
                        if (nr > -1 && nr < N && nc > -1 && nc < N
                                && open[nr][nc] && !seen[nr][nc]) {
                            dir[nr][nc]=d;
                            seen[nr][nc]=true;
                            nf.add(nr*N+nc);
                            if (goal[nr][nc]) {
                                //found tostart
                                StringBuilder fin=new StringBuilder();
                                for (int ir=nr, ic=nc; dir[ir][ic]!=-1;) {
                                    int id=dir[ir][ic];
                                    fin.append(id);//(id+2)%4);
                                    ir -= dr[id];
                                    ic -= dc[id];
                                }
                                StringBuilder out=new StringBuilder();
                                for (int i=fin.length()-1; i>-1; i--)
                                    out.append(fin.charAt(i));
                                //return inv(fin);
                                return out;
                            }
                        }
                    }
                }
                f=nf;
            }
            return null;
        }
        public Sol realized(int or, int oc) {
            //minr+=or-N, minc+=oc-N
            //minr'=minr+or-N>=0
            //maxr+or-N<N
            int er=fr+or-N, ec=fc+oc-N;
            if (er<0 || er>=N || ec<0 || ec>=N)
                return null;
            boolean good=true;
            boolean[][] open=new boolean[N][N];
            for (int i=0; i<N*N; i++)
                open[i/N][i%N]=true;
            for (int r=or, c=oc, i=0; true; i++) {
                if (r<0 || r>=N || c<0 || c>=N
                        || grid[r][c]!=0
                        || (r==N/2 && c==N/2)) {
                    good=false;
                    break;
                }
                open[r][c]=false;
                if (i==mvs.length()) {
                    if (r!=fr+or-N || c!=fc+oc-N)
                        throw new RuntimeException();
                    break;
                }
                int d=mvs.charAt(i)-'0';
                r+=dr[d];
                c+=dc[d];
            }
            if (!good)
                return null;
            StringBuilder fin=new StringBuilder(mvs);
            boolean[][] goal=new boolean[N][N];
            goal[N/2][N/2]=true;
            StringBuilder path=path(open,goal,er,ec);
            if (path!=null) {
                fin.append(path);
                return new Sol(inv(fin));
            }
            return null;
        }
    }
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            int N = Integer.parseInt(br.readLine());
            int V = Integer.parseInt(br.readLine());
            String snake = br.readLine();

            SnakeCharmer prog = new SnakeCharmer();
            char[] ret = prog.findSolution(N, V, snake);

            System.out.println(ret.length);
            for (int i = 0; i < ret.length; ++i) {
                System.out.println(ret[i]);
            }
        }
        catch (Exception e) {}
    }
}