import java.io.*;
import java.util.*;
public class RotatingNumbers {
    private static final int LEFT=0, RIGHT=1;
    private static final int[] DIRS={LEFT,RIGHT};
    private static final PrintStream P$=System.out;
    private int N, P;
    private int[][] init_grid, tmparr;
    private long END;
    private boolean bf() {
        return System.currentTimeMillis()<END;
    }
    private void rotate(List<int[]> moves, int[][] grid, int mr, int mc, int s, int dir) {
        if (moves!=null)
            moves.add(new int[] {mr,mc,s,dir});
        for (int r=mr; r<mr+s; r++)
            System.arraycopy(grid[r],mc,tmparr[r],mc,s);
        for (int r=mr; r<mr+s; r++)
            for (int c=mc; c<mc+s; c++)
                if (dir==LEFT)
                    grid[-(c-mc)+mr+s-1][(r-mr)+mc]=tmparr[r][c];
                else
                    grid[(c-mc)+mr][-(r-mr)+mc+s-1]=tmparr[r][c];
    }
    private void rotate(List<int[]> moves, int[][] grid, int[] locs, int mr, int mc, int s, int dir) {
        if (moves!=null)
            moves.add(new int[] {mr,mc,s,dir});
        for (int r=mr; r<mr+s; r++)
            System.arraycopy(grid[r],mc,tmparr[r],mc,s);
        for (int r=mr; r<mr+s; r++)
            for (int c=mc; c<mc+s; c++) {
                int nr, nc;
                if (dir==LEFT) {
                    nr=-(c-mc)+mr+s-1;
                    nc=(r-mr)+mc;
                }
                else {
                    nr=(c-mc)+mr;
                    nc=-(r-mr)+mc+s-1;
                }
                grid[nr][nc]=tmparr[r][c];
                locs[tmparr[r][c]]=nr*N+nc;
            }
    }
    private int ddist(int[][] grid, int mr, int mc, int s, int dir) {
        int out=0;
        for (int r=mr; r<mr+s; r++) {
            for (int c=mc; c<mc+s; c++) {
                int pc=grid[r][c], pcr=pc/N, pcc=pc%N;
                //(mr,mc) --> (mr+s-1,mc)
                if (dir==LEFT)
                    out += dist(pcr,pcc,-(c-mc)+mr+s-1,(r-mr)+mc);
                else //(mr,mc) --> (mr,mc+s-1)
                    out+=dist(pcr,pcc,(c-mc)+mr,-(r-mr)+mc+s-1);
                out-=dist(pcr,pcc,r,c);
            }
        }
        return out;
    }
    class Sol {
        List<int[]> moves;
        int scr;
        public Sol(List<int[]> moves, int scr) {
            this.moves=moves;
            this.scr=scr;
        }
        public String toString() {
            return "("+(moves==null?"null":moves.size())+","+scr+")";
        }
    }
    private int[][] rflip(int[][] a) {
        int[][] out=new int[N][N];
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++) {
                int t=a[i][j], rt=t/N, ct=t%N;
                out[N-1-i][j]=(N-1-rt)*N+ct;
            }
        return out;
    }
    private int[][] cflip(int[][] a) {
        int[][] out=new int[N][N];
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++) {
                int t=a[i][j], rt=t/N, ct=t%N;
                out[i][N-1-j]=rt*N+(N-1-ct);
            }
        return out;
    }
    private int[][] tp(int[][] a) {
        int[][] out=new int[N][N];
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++) {
                int t=a[i][j], rt=t/N, ct=t%N;
                out[j][i]=ct*N+rt;
            }
        return out;
    }
    private Sol bsv(int[][] init_grid) {
        int[][][] grids=new int[8][][];
        grids[0]=init_grid;
        grids[1]=rflip(grids[0]);
        for (int i=0; i<2; i++)
            grids[i+2]=cflip(grids[i]);
        for (int i=0; i<4; i++)
            grids[i+4]=tp(grids[i]);
        Sol bsol=new Sol(null,Integer.MAX_VALUE);
        int btype=-1;
        for (int i=0; i<8; i++) {
            Sol sv=solveMoves(grids[i]);
            if (sv.scr<bsol.scr) {
                bsol=sv;
                btype=i;
            }
        }
        for (int t=2; t>-1; t--) {
            if ((btype&(1<<t))!=0) {
                //undo transformation
                for (int mi=0; mi<bsol.moves.size(); mi++) {
                    int[] m=bsol.moves.get(mi);
                    bsol.moves.set(mi,t==0?new int[] {N-1-m[0]-(m[2]-1),m[1],m[2],1-m[3]}:
                            t==1?new int[] {m[0],N-1-m[1]-(m[2]-1),m[2],1-m[3]}:
                                    new int[] {m[1],m[0],m[2],1-m[3]}
                    );
                }
            }
        }
        return bsol;
    }
    private Sol solveMoves(int[][] init_grid) {
        int[][] grid=new int[N][];
        for (int i=0; i<N; i++)
            grid[i]=init_grid[i].clone();
        int[] locs=new int[N*N];
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++)
                locs[grid[i][j]]=i*N+j;
        List<int[]> moves=new ArrayList<>();
        for (int r=0; r<N-2; r++)
            for (int c=0; c<N; c++) {
                int loc=locs[r*N+c];
                int lr=loc/N, lc=loc%N;
                if (r==lr && c==lc)
                    continue;
                while (lc<c) {
                    if (lr==N-1)
                        rotate(moves,grid,locs,lr-1,lc,2,LEFT);
                    else
                        rotate(moves,grid,locs,lr,lc,2,RIGHT);
                    lc++;
                }
                while (lc>c) {
                    if (lr==N-1)
                        rotate(moves,grid,locs,lr-1,lc-1,2,RIGHT);
                    else
                        rotate(moves,grid,locs,lr,lc-1,2,LEFT);
                    lc--;
                }
                if (lr<r)
                    throw new RuntimeException();
                if (c==N-1) {
                    if (lr>r) {
                        while (lr > r + 2) {
                            rotate(moves, grid, locs, lr - 1, lc - 1, 2, LEFT);
                            lr--;
                        }
                        if (lr == r + 1)
                            rotate(moves, grid, locs, r + 1, c - 1, 2, RIGHT);
                        rotate(moves, grid, locs, r, c - 1, 2, RIGHT);
                        rotate(moves, grid, locs, r + 1, c - 1, 2, LEFT);
                        rotate(moves, grid, locs, r, c - 1, 2, LEFT);
                    }
                }
                else
                    while (lr>r) {
                        rotate(moves,grid,locs,lr-1,lc,2,RIGHT);
                        lr--;
                    }
            }
        { //r=N-2, N-1
            for (int c=0; c<N-2; c++) {
                { //r=N-2
                    int r = N - 2;
                    int loc = locs[r * N + c];
                    int lr = loc / N, lc = loc % N;
                    //P$.println(lr+" "+lc+" "+r+" "+c);
                    if (r != lr || c != lc) {
                        if (lr == N - 1) {
                            if (lc == N - 1)
                                rotate(moves, grid, locs, N - 2, lc - 1, 2, LEFT);
                            else
                                rotate(moves, grid, locs, N - 2, lc, 2, RIGHT);
                            //lr--;
                        }
                        while (lc > c) {
                            rotate(moves, grid, locs, N - 2, lc - 1, 2, LEFT);
                            lc--;
                        }
                    }
                }
                { //r=N-1
                    int r=N-1;
                    int loc = locs[r * N + c];
                    int lr = loc / N, lc = loc % N;
                    if (r != lr || c != lc) {
                        if (lc < c)
                            throw new RuntimeException();
                        if (lr == N - 2) {
                            if (lc == N - 1)
                                rotate(moves, grid, locs, N - 2, lc - 1, 2, RIGHT);
                            else
                                rotate(moves, grid, locs, N - 2, lc, 2, LEFT);
                            lr++;
                        }
                        if (lc == c + 1) {
                            rotate(moves, grid, locs, N - 2, lc, 2, LEFT);
                            lc++;
                        }
                        if (!(lc > c + 1 && lr == N - 1))
                            throw new RuntimeException();
                        rotate(moves, grid, locs, N - 2, c, 2, LEFT);
                        while (lc > c + 1) {
                            rotate(moves, grid, locs, N - 2, lc - 1, 2, RIGHT);
                            lc--;
                        }
                        rotate(moves, grid, locs, N - 2, c, 2, RIGHT);
                    }
                }
            }
        }
        {
            int btype=0, bdscr=0;
            //left 1
            int dscr=ddist(grid,N-2,N-2,2,LEFT)+1;
            if (dscr<bdscr) {
                btype=1;
                bdscr=dscr;
            }
            //left 2
            rotate(null,grid,N-2,N-2,2,LEFT);
            dscr+=ddist(grid,N-2,N-2,2,LEFT)+1;
            if (dscr<bdscr) {
                btype=2;
                bdscr=dscr;
            }
            rotate(null,grid,N-2,N-2,2,RIGHT);
            //right 1
            dscr=ddist(grid,N-2,N-2,2,RIGHT)+1;
            if (dscr<bdscr) {
                btype=3;
                bdscr=dscr;
            }
            if (btype==3)
                rotate(moves,grid,locs,N-2,N-2,2,RIGHT);
            else {
                for (int rep=0; rep<btype; rep++)
                    rotate(moves,grid,locs,N-2,N-2,2,LEFT);
            }
        }
        int scr=moves.size();
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++)
                scr+=P*dist(i,j,grid[i][j]/N,grid[i][j]%N);
        return new Sol(moves,scr);
    }
    private Sol moves(int MS) {
        if (!bf())
            return new Sol(null,Integer.MAX_VALUE);
        int[][] grid=new int[N][];
        for (int i=0; i<N; i++)
            grid[i]=init_grid[i].clone();
        List<int[]> moves=new ArrayList<>();
        int[][][][] dscrs=new int[MS+1][2][N][N];
        for (int s=2; s<=MS; s++)
            for (int dir:DIRS)
                for (int mr=0; mr+s-1<N; mr++)
                    for (int mc=0; mc+s-1<N; mc++)
                        dscrs[s][dir][mr][mc]=P*ddist(grid,mr,mc,s,dir)+(int)(Math.pow(s-1,1.5));
        int mvtot=0;
        while (bf()) {
            int bmr=-1, bmc=-1, bs=-1, bdir=-1, bscr=0;
            for (int s=2; s<=MS; s++)
                for (int dir:DIRS)
                    for (int mr=0; mr+s-1<N; mr++)
                        for (int mc=0; mc+s-1<N; mc++) {
                            int scr=dscrs[s][dir][mr][mc];
                            if (scr<bscr) {
                                bscr=scr;
                                bmr=mr;
                                bmc=mc;
                                bs=s;
                                bdir=dir;
                            }
                        }
            if (bscr>=0)
                break;
            mvtot+=(int)Math.pow(bs-1,1.5);
            rotate(moves,grid,bmr,bmc,bs,bdir);
            for (int s=2; s<=MS; s++)
                for (int dir:DIRS)
                    for (int mr=Math.max(0,bmr-s+1); mr<Math.min(N-s+1,bmr+bs); mr++)
                        for (int mc=Math.max(0,bmc-s+1); mc<Math.min(N-s+1,bmc+bs); mc++)
                            dscrs[s][dir][mr][mc]=P*ddist(grid,mr,mc,s,dir)+(int)(Math.pow(s-1,1.5));
        }
        Sol bsol=new Sol(null,Integer.MAX_VALUE);
        for (int len=moves.size(); len>-1; len--) {
            if (len<moves.size()) {
                if (!bf())
                    break;
                int[] m = moves.get(len);
                mvtot -= (int) Math.pow(m[2] - 1, 1.5);
                rotate(null, grid, m[0], m[1], m[2], 1 - m[3]);
            }
            Sol sv = bsv(grid);
            int scr = mvtot + sv.scr;
            if (scr < bsol.scr) {
                List<int[]> mvs = new ArrayList<>();
                for (int i = 0; i < len; i++)
                    mvs.add(moves.get(i).clone());
                mvs.addAll(sv.moves);
                bsol = new Sol(mvs, scr);
            }
        }
        return bsol;
    }
    public String[] findSolution(int N, int P, int[][] grid_arg) {
        END=System.currentTimeMillis()+9500;
        this.N=N;
        this.P=P;
        tmparr=new int[N][N];
        init_grid=new int[N][N];
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++)
                init_grid[i][j]=grid_arg[i][j]-1;
        Sol[] sols={moves(5),moves(4),moves(3)};
        String[] names={"g5_8v","g4_8v","g3_8v"};
        for (int i=0; i<sols.length; i++)
            P$.println(names[i]+":"+sols[i]);
        Sol bs=sols[0];
        for (int i=1; i<sols.length; i++)
            if (sols[i].scr<bs.scr)
                bs=sols[i];
        for (int n=6; n<=N && bf(); n++) {
            Sol s=moves(n);
            P$.println("g"+n+"_8v:"+s);
            if (s.scr<bs.scr)
                bs=s;
        }
        String[] out=new String[bs.moves.size()];
        for (int i=0; i<out.length; i++) {
            int[] m=bs.moves.get(i);
            out[i]=m[0]+" "+m[1]+" "+m[2]+" "+(m[3]==LEFT?"L":"R");
        }
        return out;
    }
    private static int dist(int r0, int c0, int r1, int c1) {
        return Math.abs(r0-r1)+Math.abs(c0-c1);
    }
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            int N = Integer.parseInt(br.readLine());
            int P = Integer.parseInt(br.readLine());
            int[][] grid = new int[N][N];
            for (int r=0; r<N; r++)
                for (int c=0; c<N; c++)
                    grid[r][c]=Integer.parseInt(br.readLine());
            RotatingNumbers prog = new RotatingNumbers();
            String[] ret = prog.findSolution(N, P, grid);
            System.out.println(ret.length);
            for (int i = 0; i < ret.length; i++)
                System.out.println(ret[i]);
        }
        catch (Exception e) {}
    }
}