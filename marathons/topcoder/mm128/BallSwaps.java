import java.util.*;
import java.io.*;
public class BallSwaps {
    private static final PrintStream P$=System.err;
    private static final long INIT_TL=2000, TOT_TL=9250;
    private static final int[] dr={1,0,-1,0}, dc={0,1,0,-1};
    private static void swap(int[][] A, List<String> mvs, int r0, int c0, int r1, int c1) {
        int tmp=A[r0][c0];
        A[r0][c0]=A[r1][c1];
        A[r1][c1]=tmp;
        mvs.add(r0+" "+c0+" "+r1+" "+c1);
    }
    private static int[][] copy(int[][] A) {
        int[][] out=new int[A.length][];
        for (int i=0; i<A.length; i++)
            out[i]=Arrays.copyOf(A[i],A[i].length);
        return out;
    }
    private static List<int[]> perms(int N) {
        List<int[]> out=new ArrayList<>();
        if (N==1) out.add(new int[] {0});
        else {
            List<int[]> help=perms(N-1);
            for (int[] p:help) for (int i=N-1; i>-1; i--) {
                int[] n=new int[N];
                System.arraycopy(p,0,n,0,i);
                n[i]=N-1;
                System.arraycopy(p,i,n,i+1,N-1-i);
                out.add(n);
            }
        }
        return out;
    }
    //component counter taken from tester
    private int N, C;
    private int componentCount(int[][] grid) {
        int[][] tmp=copy(grid);
        int count=0;
        for (int r=0; r<N; r++) for (int c=0; c<N; c++)
            if (tmp[r][c]!=-1) {
                dfs(r,c,tmp[r][c],tmp);
                count++;
            }
        return count;
    }

    private void dfs(int r, int c, int color, int[][] grid)
    {
        if (r<0||r>=N||c<0||c>=N||grid[r][c]!=color) return;
        grid[r][c]=-1;    //set to used
        dfs(r+1,c,color,grid);
        dfs(r-1,c,color,grid);
        dfs(r,c+1,color,grid);
        dfs(r,c-1,color,grid);
    }
    private int[][] verts;
    private boolean[] seen;
    private List<int[]> solPath(int[][] grid, int row, int clm, int tcol) {
        if (tcol<0||tcol>=C) throw new RuntimeException();
        List<int[]> pts=null;
        if (grid[row][clm]==tcol) pts=new ArrayList<>(Arrays.asList(new int[] {row,clm}));
        else {
            int sz=0;
            verts[sz++]=new int[] {row,clm,-1};
            seen[row*N+clm]=true;
            BFS: for (int i=0; i<sz; i++) {
                int r=verts[i][0], c=verts[i][1];
                for (int d=0; d<4; d++) {
                    int nr=r+dr[d], nc=c+dc[d];
                    if (0<=nr&&nr<N&&0<=nc&&nc<N&&!seen[nr*N+nc]&&grid[nr][nc]!=C) {
                        seen[nr*N+nc]=true;
                        verts[sz++]=new int[] {nr,nc,i};
                        if (grid[nr][nc]==tcol) {
                            pts=new ArrayList<>();
                            for (int vi=sz-1; vi>-1; vi=verts[vi][2])
                                pts.add(Arrays.copyOfRange(verts[vi],0,2));
                            break BFS;
                        }
                    }
                }
            }
            for (int i=0; i<sz; i++) {
                int[] v=verts[i];
                seen[v[0]*N+v[1]]=false;
            }
        }
        if (pts==null) {
            P$.println(row+" "+clm+" "+tcol);
            for (int r=0; r<N; r++) {
                for (int c=0; c<N; c++)
                    P$.print(grid[r][c]);
                P$.println();
            }
            throw new NullPointerException();
        }
        return pts;
    }
    private void moveAlong(int[][] grid, List<String> mvs, List<int[]> pts) {
        for (int pi=0; pi<pts.size()-1; pi++) {
            int[] p0=pts.get(pi), p1=pts.get(pi+1);
            if (grid[p0[0]][p0[1]]==C||grid[p1[0]][p1[1]]==C)
                throw new RuntimeException();
            swap(grid,mvs,p0[0],p0[1],p1[0],p1[1]);
        }
        int[] p=pts.get(pts.size()-1);
        grid[p[0]][p[1]]=C;
    }
    private String[] solve(int[][] start, int[][] end) {
        int[][] grid=copy(start);
        List<String> mvs=new ArrayList<>();
        for (int ca=0; ca<N-1-ca; ca++) {
            moveAlong(grid,mvs,solPath(grid,ca,ca,end[ca][ca]));
            moveAlong(grid,mvs,solPath(grid,N-1-ca,ca,end[N-1-ca][ca]));
            moveAlong(grid,mvs,solPath(grid,ca,N-1-ca,end[ca][N-1-ca]));
            moveAlong(grid,mvs,solPath(grid,N-1-ca,N-1-ca,end[N-1-ca][N-1-ca]));
            Set<Integer> pending=new HashSet<>();
            for (int cb=ca+1; cb<N-1-ca; cb++) {
                pending.add(ca*N+cb);
                pending.add(cb*N+ca);
                pending.add((N-1-ca)*N+cb);
                pending.add(cb*N+(N-1-ca));
            }
            while (pending.size()>0) {
                int bpt=-1;
                List<int[]> bpath=null;
                for (int pt:pending) {
                    List<int[]> pts=solPath(grid,pt/N,pt%N,end[pt/N][pt%N]);
                    if (bpath==null||pts.size()<bpath.size()) {
                        bpath=pts;
                        bpt=pt;
                    }
                }
                moveAlong(grid,mvs,bpath);
                pending.remove(bpt);
            }
        }
        return mvs.toArray(new String[0]);
    }
    public String[] solution(int N, int C, int[][] grid) {
        long st=System.currentTimeMillis();
        this.N=N; this.C=C;
        seen=new boolean[N*N];
        verts=new int[N*N][];
        int[] freq=new int[C];
        for (int[] row:grid) for (int v:row) freq[v]++;
        int[] locOrd=new int[N*N]; {
            boolean[] seen=new boolean[N*N];
            for (int r=0, c=0, d=0, i=0; i<N*N; i++) {
                locOrd[i]=r*N+c;
                seen[r*N+c]=true;
                if (d==0) {
                    r++;
                    if (r==N||seen[r*N+c]) {
                        r--;
                        d++;
                        c++;
                    }
                }
                else if (d==1) {
                    c++;
                    if (c==N||seen[r*N+c]) {
                        c--;
                        d++;
                        r--;
                    }
                }
                else if (d==2) {
                    r--;
                    if (r==-1||seen[r*N+c]) {
                        r++;
                        d++;
                        c--;
                    }
                }
                else {
                    c--;
                    if (c==-1||seen[r*N+c]) {
                        c++;
                        d=0;
                        r++;
                    }
                }
            }
        }
        String[] sol=null;
        int[][] end=null;
        List<int[]> perms=perms(C);
        for (int[] p:perms) {
            if (System.currentTimeMillis()-st>INIT_TL) break;
            int[][] nend=new int[N][N];
            for (int ci=0, li=0; ci<C; ci++) {
                int col=p[ci];
                for (int rep=0; rep<freq[col]; rep++, li++) {
                    int l=locOrd[li];
                    nend[l/N][l%N]=col;
                }
            }
            String[] nsol=solve(grid,nend);
            if (sol==null||nsol.length<sol.length) {
                sol=nsol;
                end=nend;
            }
        }
        //TODO: DO SA W/ HEURISTIC SCORE, THNE HC ON REAL SCORE
        SplittableRandom rnd=new SplittableRandom(1);
        long STEPSZ=1000, MARK=0;
        List<List<List<int[]>>> touchers=new ArrayList<>();
        boolean eval_touchers=true;
        //touchers.get(a).get(b)=list of all cells of color a adjacent to a cell of color b in array end[][]
        long HC_TL; {
            long time0=System.currentTimeMillis()-st;
            P$.println("time taken for initial solution="+time0);
            HC_TL=TOT_TL-time0;
        }
        st=System.currentTimeMillis();
        double avgtries=0, trials=0;
        for (long REPS=0, ACCN=0;; REPS++) {
            long TIME=System.currentTimeMillis()-st;
            if (TIME>=MARK||TIME>=HC_TL) {
                P$.printf("%8d%8d%8d%8d%n",TIME,REPS,ACCN,sol.length);
                MARK+=STEPSZ;
            }
            if (TIME>=HC_TL) break;
            if (eval_touchers) {
                List<List<Set<int[]>>> tmp=new ArrayList<>();
                for (int a=0; a<C; a++) {
                    tmp.add(new ArrayList<>());
                    for (int b=0; b<C; b++)
                        tmp.get(a).add(new HashSet<>());
                }
                for (int r=0; r<N; r++) for (int c=0; c<N; c++)
                    for (int d=0; d<4; d++) {
                        int nr=r+dr[d], nc=c+dc[d];
                        if (0<=nr&&nr<N&&0<=nc&&nc<N&&end[nr][nc]!=end[r][c])
                            tmp.get(end[r][c]).get(end[nr][nc]).add(new int[] {r,c});
                    }
                touchers.clear();
                for (int a=0; a<C; a++) {
                    touchers.add(new ArrayList<>());
                    for (int b=0; b<C; b++)
                        touchers.get(a).add(new ArrayList<>(tmp.get(a).get(b)));
                }
                eval_touchers=false;
            }
            int[] pa=null, pb=null; int ca=-1, cb=-1; for (int tries=0; tries<100; tries++, avgtries++) {
                ca=rnd.nextInt(C); cb=rnd.nextInt(C-1); if (cb>=ca) cb++;
                List<int[]> l0=touchers.get(ca).get(cb), l1=touchers.get(cb).get(ca);
                if (l0.size()>0&&l1.size()>0) {
                    pa=l0.get(rnd.nextInt(l0.size()));
                    pb=l1.get(rnd.nextInt(l1.size()));
                    end[pa[0]][pa[1]]=cb; end[pb[0]][pb[1]]=ca;
                    if (componentCount(end)==C)
                        break;
                    else {
                        end[pa[0]][pa[1]]=ca; end[pb[0]][pb[1]]=cb;
                    }
                }
            }
            trials++;
            if (pa==null) continue;
            String[] nsol=solve(grid,end);
            if (nsol.length<=sol.length) {
                sol=nsol;
                eval_touchers=true;
                ACCN++;
            }
            else {
                end[pa[0]][pa[1]]=ca; end[pb[0]][pb[1]]=cb;
                eval_touchers=false;
            }
        }
        P$.println("avg tries per mutation="+avgtries/trials);
        return sol;
    }
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        int N = Integer.parseInt(br.readLine());
        int C = Integer.parseInt(br.readLine());

        int[][] grid=new int[N][N];
        for (int r=0; r<N; r++)
            for (int c=0; c<N; c++)
                grid[r][c] = Integer.parseInt(br.readLine());

        String[] sol=new BallSwaps().solution(N,C,grid);
        System.out.println(sol.length);
        for (String s:sol) System.out.println(s);
    }
}