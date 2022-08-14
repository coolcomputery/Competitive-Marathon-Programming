import java.io.*;
import java.util.*;
public class GlowingBacteria {
    //private static final int[] maxC_givenK={0,0,0,0,2,3,3,3,4,4,5};
    private int N, C, D, K;
    private int[] target;
    private int[][] boards;
    private int boardScr;
    private int nextCol(int[] board, int r, int c) {
        int[] cnts=new int[C];
        //dr*dr+dc*dc<=K
        int maxdr=(int)Math.sqrt(K);
        for (int dr=-maxdr; dr<=maxdr; dr++) {
            int maxdc=(int)Math.sqrt(K-dr*dr);
            for (int dc=-maxdc; dc<=maxdc; dc++) {
                int nr=r+dr, nc=c+dc;
                if (nr>-1 && nr<N && nc>-1 && nc<N && (dr!=0 || dc!=0))
                    cnts[board[nr*N+nc]]++;
            }
        }
        int min=Integer.MAX_VALUE, mcol=-1, copies=0;
        for (int i=0; i<C; i++) {
            if (cnts[i]<min && cnts[i]>0) {
                mcol=i;
                min=cnts[i];
                copies=1;
            }
            else if (cnts[i]==min)
                copies++;
        }
        return copies==1?mcol:board[r*N+c];
    }
    private void init(int[] st) {
        boards=new int[D+1][];
        boards[0]=st.clone();
        for (int d=0; d<D; d++) {
            boards[d+1]=new int[N*N];
            for (int l=0; l<N*N; l++) {
                int r=l/N, c=l%N;
                boards[d+1][l]=nextCol(boards[d],r,c);
            }
        }
        boardScr=0;
        for (int i=0; i<N*N; i++)
            if (boards[D][i]==target[i])
                boardScr++;
    }
    private void update(int r0, int c0, int h, int w) {
        for (int d=1; d<=D; d++) {
            int sqDist=K*d*d;
            for (int nr=Math.max(0,r0-(int)Math.sqrt(sqDist)); nr<Math.min(N,r0+h+(int)Math.sqrt(sqDist)); nr++) {
                for (int nc = Math.max(0,c0-(int)Math.sqrt(sqDist)); nc < Math.min(N,c0+w+(int)Math.sqrt(sqDist)); nc++) {
                    int ocol=boards[d][nr*N+nc];
                    boards[d][nr * N + nc] = nextCol(boards[d - 1], nr, nc);
                    if (d==D)
                        boardScr+=(boards[d][nr*N+nc]==target[nr*N+nc]?1:0)-(ocol==target[nr*N+nc]?1:0);
                }
            }
        }
    }
    public char[] findSolution(int N, int C, int D, int K, char[] target_arg) {
        /*if (C>maxC_givenK[K])
            return target_arg;*/
        long END=System.currentTimeMillis()+19700;
        this.N=N;
        this.C=C;
        this.D=D;
        this.K=K;
        int[] start=new int[N*N];
        target=new int[N*N];
        for (int i=0; i<N*N; i++)
            target[i]=target_arg[i]-'0';
        SplittableRandom rnd=new SplittableRandom(1);
        for (int i=0; i<N*N; i++)
            start[i]=//target[i];
                    rnd.nextInt(C);//-1;
        /*int left=N*N;
        while (left>0) {
            int r=rnd.nextInt(N), c=rnd.nextInt(N);
            int w=rnd.nextInt(N/4)+1, h=rnd.nextInt(N/4)+1;
            int col=rnd.nextInt(C);
            for (int r1=r; r1<N && r1<r+h; r1++)
                for (int c1=c; c1<N && c1<c+w; c1++) {
                    if (init[r1 * N + c1] == -1)
                        left--;
                    init[r1*N+c1]=col;
                }
        }*/
        init(start);
        int scr=boardScr;
        String form="%8d%8d%8.2f%n";
        long TIME=System.currentTimeMillis(), TL=END-TIME, LOG=TIME+TL/10;
        PrintStream PRINT=System.out;
        int REPS=0;
        double T=K, temp=T;
        int[] safe=new int[N*N];
        while (true) {
            TIME=System.currentTimeMillis();
            if (TIME>END)
                break;
            temp=T*(END-TIME)/TL;
            int row=rnd.nextInt(N), clm=rnd.nextInt(N);
            int w=rnd.nextInt(N/4)+1, h=rnd.nextInt(N/4)+1;
            int col=rnd.nextInt(C);
            for (int r1=row; r1<N && r1<row+h; r1++)
                for (int c1=clm; c1<N && c1<clm+w; c1++) {
                    safe[r1*N+c1]=boards[0][r1*N+c1];
                    boards[0][r1*N+c1]=col;
                }
            update(row,clm,h,w);
            int nscr=boardScr;
            if (nscr>=scr || rnd.nextDouble()<Math.exp((nscr-scr)/temp))
                scr=nscr;
            else {
                for (int r1=row; r1<N && r1<row+h; r1++)
                    for (int c1=clm; c1<N && c1<clm+w; c1++)
                        boards[0][r1*N+c1]=safe[r1*N+c1];
                update(row,clm,h,w);
            }
            REPS++;
            if (TIME>LOG) {
                LOG+=TL/10;
                PRINT.printf(form,REPS,scr,temp);
            }
        }
        PRINT.printf(form,REPS,scr,temp);
        char[] out=new char[N*N];
        for (int i=0; i<N*N; i++)
            out[i]=(char)(boards[0][i]+48);
        PRINT.println("measured time="+(System.currentTimeMillis()-(END-9700)));
        return out;
    }
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            int N = Integer.parseInt(br.readLine());
            int C = Integer.parseInt(br.readLine());
            int D = Integer.parseInt(br.readLine());
            int K = Integer.parseInt(br.readLine());
            char[] grid = new char[N*N];
            for (int i=0; i<N*N; i++) grid[i]=br.readLine().charAt(0);
            GlowingBacteria gb = new GlowingBacteria();
            char[] ret = gb.findSolution(N, C, D, K, grid);
            System.out.println(ret.length);
            for (int i = 0; i < ret.length; i++) System.out.println(ret[i]);
        }
        catch (Exception e) {}
    }
}