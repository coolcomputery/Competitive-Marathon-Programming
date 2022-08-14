import java.io.*;
import java.util.*;
public class DanceFloor {
    private static final PrintStream P$=System.err;
    private static final char[] dirs={'R','U','L','D'};
    private static char inv(char d) {
        return d=='L'?'R':d=='R'?'L':d=='U'?'D':d=='D'?'U':null;
    }
    private int N, C;
    private int[][][] tcols;
    private int[][] board;
    private int[][] marks;
    private int region(int[] uf, int i) {
        if (uf[i]==i)
            return i;
        else {
            uf[i]=region(uf,uf[i]);
            return uf[i];
        }
    }
    private boolean merge(int[] uf, int i, int j) {
        boolean diff=region(uf,i)!=region(uf,j);
        uf[region(uf,i)]=region(uf,j);
        return diff;
    }
    private int scr(int[][] tidxs) {
        int[] cnt=new int[C], uf=new int[N*N];
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++) {
                uf[i*N+j]=i*N+j;
                int col=board[i][j];//tcols[i][j][tidxs[i][j]];
                cnt[col]++;
                if (i>0 && board[i-1][j]//tcols[i-1][j][tidxs[i-1][j]]
                                ==col
                        && merge(uf,i*N+j,(i-1)*N+j))
                    cnt[col]--;
                if (j>0 && board[i][j-1]//tcols[i][j-1][tidxs[i][j-1]]
                                ==col
                        && merge(uf,i*N+j,i*N+(j-1)))
                    cnt[col]--;
            }
        int out=0;
        for (int f:cnt)
            out+=f*f;
        return out;
    }
    private void paint(int[][] tidxs, char[][] moves, int d, int k, int dl) {
        int sx=marks[d][k], sy=marks[d][k+1];
        for (int t=marks[d][k+2]; t<marks[d][k+5]; t++) {
            char c=moves[d][t];
            if (c=='R') sx++;
            else if (c=='L') sx--;
            else if (c=='D') sy++;
            else if (c=='U') sy--;
            if (c!='-') {
                tidxs[sy][sx]=((tidxs[sy][sx]+dl)%C+C)%C;
                board[sy][sx]=tcols[sy][sx][tidxs[sy][sx]];
            }
        }
    }
    private boolean bounded(char[][] moves, int d, int k) {
        int sx=marks[d][k], sy=marks[d][k+1];
        for (int t=marks[d][k+2]; t<marks[d][k+5]; t++) {
            char c=moves[d][t];
            if (c=='R') sx++;
            else if (c=='L') sx--;
            else if (c=='D') sy++;
            else if (c=='U') sy--;
            if (sx<0 || sx>=N || sy<0 || sy>=N)
                return false;
        }
        return true;
    }
    public String[] findSolution(int N, int C, int D, int S, String[][] tileColors, int[][] marks) {
        long END=System.currentTimeMillis()+9500;
        this.N=N;
        this.C=C;
        this.tcols=new int[N][N][C];
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++)
                for (int c=0; c<C; c++)
                    tcols[i][j][c]=tileColors[i][j].charAt(c)-'0';
        this.marks=new int[D][];
        for (int i=0; i<D; i++)
            this.marks[i]=marks[i].clone();
        char[][] out=new char[D][S];
        int[][] pathlen=new int[D][];
        List<List<List<int[]>>> diffpairs=new ArrayList<>(),
                invpairs=new ArrayList<>();
        SplittableRandom rnd=new SplittableRandom(1);
        for (int d=0; d<D; d++) {
            pathlen[d]=new int[marks[d].length/3-1];
            diffpairs.add(new ArrayList<>());
            invpairs.add(new ArrayList<>());
            for (int k=0; k<marks[d].length-3; k+=3) {
                int x=marks[d][k], y=marks[d][k+1], t0=marks[d][k+2];
                int nx=marks[d][k+3], ny=marks[d][k+4], nt=marks[d][k+5];
                int dist=d(x,y,nx,ny);
                do {
                    int e=rnd.nextInt((nt-t0-dist)/2+1);
                    for (int i=0; i<nt-t0;)
                        if (i<Math.abs(nx-x)) {
                            out[d][i+t0]=x<nx?'R':'L';
                            i++;
                        }
                        else if (i<dist) {
                            out[d][i+t0]=y<ny?'D':'U';
                            i++;
                        }
                        else if (i<dist+2*e) {
                            char dir=dirs[rnd.nextInt(4)];
                            out[d][i+t0]=dir;
                            out[d][i+1+t0]=inv(dir);
                            i+=2;
                        }
                        else {
                            out[d][i+t0]='-';
                            i++;
                        }
                    for (int i=dist+2*e-1; i>0; i--) {
                        int j=rnd.nextInt(i+1);
                        char tmp=out[d][i+t0];
                        out[d][i+t0]=out[d][j+t0];
                        out[d][j+t0]=tmp;
                    }
                    pathlen[d][k/3]=dist+2*e;
                }
                while (!bounded(out,d,k));
                diffpairs.get(d).add(new ArrayList<>());
                invpairs.get(d).add(new ArrayList<>());
                for (int i=1; i<pathlen[d][k/3]; i++)
                    for (int j=0; j<i; j++) {
                        if (out[d][i+t0]!=out[d][j+t0]) {
                            diffpairs.get(d).get(k/3).add(new int[] {j,i});
                            if (out[d][i+t0]==inv(out[d][j+t0]))
                                invpairs.get(d).get(k/3).add(new int[] {j,i});
                        }
                    }
            }
        }
        int[][] tidxs=new int[N][N];
        board=new int[N][N];
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++)
                board[i][j]=tcols[i][j][0];
        for (int d=0; d<D; d++)
            for (int k=0; k<marks[d].length-3; k+=3)
                paint(tidxs,out,d,k,1);
        int scr=scr(tidxs);
        int TREPS=0, TACCN=0;
        int[] REPS=new int[5], ACCN=new int[5];
        double T=scr/1000.0, temp=T;
        String form="%8d%40s%8d%40s%10d%15.6f%n";
        P$.printf(form,TREPS,Arrays.toString(REPS),TACCN,Arrays.toString(ACCN),scr,temp);
        List<int[]> segs=new ArrayList<>();
        for (int d=0; d<D; d++)
            for (int k=0; k<marks[d].length-3; k+=3)
                segs.add(new int[] {d,k});
        char[] tmpmoves=new char[S];
        long TIME=System.currentTimeMillis(), TL=END-TIME, LOG=TIME+TL/10;
        while (true) {
            TIME=System.currentTimeMillis();
            if (TIME>END)
                break;
            double alpha=1.0*(END-TIME)/TL;
            temp=T*alpha*alpha;
            int[] s$=segs.get(rnd.nextInt(segs.size()));
            int d$=s$[0], k$=s$[1];
            int dist=d(marks[d$][k$],marks[d$][k$+1],marks[d$][k$+3],marks[d$][k$+4]);
            int t0=marks[d$][k$+2],
                    t1=t0+pathlen[d$][k$/3], te=marks[d$][k$+5];
            int npathlen=t1-t0;
            paint(tidxs,out,d$,k$,-1);
            System.arraycopy(out[d$],t0,tmpmoves,t0,te-t0);
            List<int[]> dps=diffpairs.get(d$).get(k$/3),
                    ips=invpairs.get(d$).get(k$/3);
            int type=-1;
            if (rnd.nextDouble()<0.5 || t0+dist+2>te) {
                List<Integer> tas=new ArrayList<>();
                for (int t=t0; t<t1-1; t++)
                    if (out[d$][t]!=out[d$][t+1])
                        tas.add(t);
                int ta$, tb$;
                if (rnd.nextDouble()<1-alpha && tas.size()>0) {
                    int limit=10;
                    while (limit>0) {
                        ta$=tas.get(rnd.nextInt(tas.size()));
                        tb$=ta$+1;
                        type=0;
                        char tmp=out[d$][ta$];
                        out[d$][ta$]=out[d$][tb$];
                        out[d$][tb$]=tmp;
                        if (bounded(out,d$,k$))
                            break;
                        else {
                            type=-1;
                            tmp=out[d$][ta$];
                            out[d$][ta$]=out[d$][tb$];
                            out[d$][tb$]=tmp;
                        }
                        limit--;
                    }
                }
                if (type==-1 && t1-t0>1 && dps.size()>0) {
                    int limit=10;
                    while (limit>0) {
                        int[] p$=dps.get(rnd.nextInt(dps.size()));
                        ta$=p$[0]+t0;
                        tb$=p$[1]+t0;
                        if (out[d$][ta$]!=out[d$][tb$]) {
                            type=1;
                            char tmp=out[d$][ta$];
                            out[d$][ta$]=out[d$][tb$];
                            out[d$][tb$]=tmp;
                            if (bounded(out,d$,k$))
                                break;
                            else {
                                type=-1;
                                tmp=out[d$][ta$];
                                out[d$][ta$]=out[d$][tb$];
                                out[d$][tb$]=tmp;
                            }
                        }
                        else {
                            P$.println();
                            for (int i=t0; i<te; i++)
                                P$.print(out[d$][i]);
                            P$.println("\n"+d$+" "+k$/3+" "+Arrays.toString(p$)+" "+out[d$][ta$]+" "+out[d$][tb$]);
                            throw new RuntimeException("TREPS="+TREPS+" limit="+limit);
                        }
                        limit--;
                    }
                }
            }
            if (type==-1 && t0+dist+2<=te) {
                int ta$, tb$;
                if ((rnd.nextDouble()<0.5 && t1+2<=te) || t1-t0==dist) {
                    //add length
                    while (true) {
                        if (rnd.nextDouble()<alpha) {
                            ta$=rnd.nextInt(t1+2-t0)+t0;
                            tb$=rnd.nextInt(t1+1-t0)+t0;
                            if (tb$>=ta$)
                                tb$++;
                            if (ta$>tb$) {
                                int t=ta$;
                                ta$=tb$;
                                tb$=t;
                            }
                            type=2;
                        }
                        else {
                            ta$=rnd.nextInt(t1+1-t0)+t0;
                            tb$=ta$+1;
                            type=3;
                        }
                        System.arraycopy(tmpmoves,t0,out[d$],t0,ta$-t0);
                        char d=dirs[rnd.nextInt(4)];
                        out[d$][ta$]=d;
                        System.arraycopy(tmpmoves,ta$,out[d$],ta$+1,tb$-(ta$+1));
                        out[d$][tb$]=inv(d);
                        System.arraycopy(tmpmoves,tb$-1,out[d$],tb$+1,te-(tb$+1));
                        if (bounded(out,d$,k$))
                            break;
                    }
                    npathlen+=2;
                }
                else if (ips.size()>0) {
                    //remove length
                    while (true) {
                        int[] p$=ips.get(rnd.nextInt(ips.size()));
                        ta$=p$[0]+t0;
                        tb$=p$[1]+t0;
                        //tb$=nums.get(rnd.nextInt(nums.size()));
                        if (ta$==tb$)
                            throw new RuntimeException();
                        if (ta$>tb$) {
                            int t=ta$;
                            ta$=tb$;
                            tb$=t;
                        }
                        if (tmpmoves[ta$]!=inv(tmpmoves[tb$])){
                            P$.println();
                            for (int i=t0; i<te; i++)
                                P$.print(out[d$][i]);
                            P$.println("\n"+d$+" "+k$/3+" "+Arrays.toString(p$)+" "+out[d$][ta$]+" "+out[d$][tb$]);
                            P$.println(ta$+" "+(p$[0]+t0)+" "+tb$+" "+(p$[1]+t0));
                            for (int[] p:ips)
                                P$.println(Arrays.toString(p));
                            throw new RuntimeException("TREPS="+TREPS);
                        }
                        System.arraycopy(tmpmoves,t0,out[d$],t0,ta$-t0);
                        System.arraycopy(tmpmoves,ta$+1,out[d$],ta$,tb$-(ta$+1));
                        System.arraycopy(tmpmoves,tb$+1,out[d$],tb$-1,te-(tb$+1));
                        out[d$][te-2]=out[d$][te-1]='-';
                        if (bounded(out,d$,k$))
                            break;
                    }
                    type=4;
                    npathlen-=2;
                }
            }
            if (type>-1) {
                paint(tidxs,out,d$,k$,1);
                int nscr=scr(tidxs);
                if (nscr<=scr || rnd.nextDouble()<Math.exp((scr-nscr)/temp)) {
                    scr=nscr;
                    pathlen[d$][k$/3]=npathlen;
                    /*for (int i=0; i<pathlen[d$][k$/3]; i++)
                        P$.print(out[d$][i+t0]);
                    P$.println();*/
                    dps.clear();
                    ips.clear();
                    for (int i=0; i<pathlen[d$][k$/3]; i++)
                        for (int j=0; j<i; j++) {
                            if (out[d$][i+t0]!=out[d$][j+t0]) {
                                dps.add(new int[] {j,i});
                                if (out[d$][i+t0]==inv(out[d$][j+t0]))
                                    ips.add(new int[] {j,i});
                            }
                        }
                    ACCN[type]++;
                    TACCN++;
                }
                else {
                    paint(tidxs,out,d$,k$,-1);
                    System.arraycopy(tmpmoves,t0,out[d$],t0,te-t0);
                    paint(tidxs,out,d$,k$,1);
                }
                REPS[type]++;
            }
            else
                paint(tidxs,out,d$,k$,1);
            TREPS++;
            if (TIME>LOG) {
                LOG+=TL/10;
                P$.printf(form,TREPS,Arrays.toString(REPS),TACCN,Arrays.toString(ACCN),scr,temp);
            }
        }
        P$.printf(form,TREPS,Arrays.toString(REPS),TACCN,Arrays.toString(ACCN),scr,temp);
        String[] ret = new String[S];
        for (int s=0; s<S; s++) {
            StringBuilder str=new StringBuilder();
            for (int d=0; d<D; d++)
                str.append(out[d][s]);
            ret[s]=str.toString();
        }
        return ret;
    }
    private static int d(int x0, int y0, int x1, int y1) {
        return Math.abs(x1-x0)+Math.abs(y1-y0);
    }
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            int N = Integer.parseInt(br.readLine());
            int C = Integer.parseInt(br.readLine());
            int D = Integer.parseInt(br.readLine());
            int S = Integer.parseInt(br.readLine());
            String[][] tileColors = new String[N][N];
            for (int y = 0; y < N; y++) {
                for (int x = 0; x < N; x++) {
                    tileColors[y][x] = br.readLine();
                }
            }
            int[][] marks = new int[D][];
            for (int i = 0; i < D; i++) {
                int numMarks = Integer.parseInt(br.readLine());
                marks[i] = new int[3 * numMarks];
                String[] s = br.readLine().split(" ");
                for (int j = 0; j < 3 * numMarks; j++) {
                    marks[i][j] = Integer.parseInt(s[j]);
                }
            }
            DanceFloor sol = new DanceFloor();
            String[] ret = sol.findSolution(N, C, D, S, tileColors, marks);
            System.out.println(ret.length);
            for (int i = 0; i < ret.length; i++) {
                System.out.println(ret[i]);
            }
            System.out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}