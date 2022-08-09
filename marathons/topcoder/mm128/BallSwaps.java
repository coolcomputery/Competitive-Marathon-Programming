import java.util.*;
import java.io.*;
public class BallSwaps {
    private static final long TL=9250;
    private static final int[] dr={1,0,-1,0}, dc={0,1,0,-1};
    private static void swap(int[][] A, List<String> mvs, int r0, int c0, int r1, int c1) {
        int tmp=A[r0][c0];
        A[r0][c0]=A[r1][c1];
        A[r1][c1]=tmp;
        mvs.add(r0+" "+c0+" "+r1+" "+c1);
    }
    private int N, C;
    //component counter taken from tester
    private int componentCount(int[][] grid) {
        int[][] tmp=new int[N][];
        for (int i=0; i<N; i++) tmp[i]=Arrays.copyOf(grid[i],N);
        int count=0;
        for (int r=0; r<N; r++) for (int c=0; c<N; c++)
            if (tmp[r][c]!=-1) {
                dfs(r,c,tmp[r][c],tmp);
                count++;
            }
        return count;
    }
    private void dfs(int r, int c, int color, int[][] grid) {
        if (r<0||r>=N||c<0||c>=N||grid[r][c]!=color) return;
        grid[r][c]=-1;    //set to used
        dfs(r+1,c,color,grid);
        dfs(r-1,c,color,grid);
        dfs(r,c+1,color,grid);
        dfs(r,c-1,color,grid);
    }
    private void solveLoc(int[][] grid, List<String> mvs, int row, int clm, int tcol) {
        if (grid[row][clm]==tcol) {
            grid[row][clm]=C;
            return;
        }
        //grid[r][c]==C means that cell (r,c) is solved and should not be moved
        //MUTATES grid
        //brings closest cell (r,c) s.t. grid[r][c]==tcol to location (row,clm)
        if (tcol<0||tcol>=C) throw new RuntimeException();
        List<int[]> verts=new ArrayList<>();
        verts.add(new int[] {row,clm,-1});
        List<int[]> pts=null;
        boolean[][] seen=new boolean[N][N];
        seen[row][clm]=true;
        BFS: for (int i=0; i<verts.size(); i++) {
            int r=verts.get(i)[0], c=verts.get(i)[1];
            for (int d=0; d<4; d++) {
                int nr=r+dr[d], nc=c+dc[d];
                //System.out.println(nr+" "+nc);
                if (0<=nr&&nr<N&&0<=nc&&nc<N&&!seen[nr][nc]&&grid[nr][nc]!=C) {
                    seen[nr][nc]=true;
                    verts.add(new int[] {nr,nc,i});
                    if (grid[nr][nc]==tcol) {
                        pts=new ArrayList<>();
                        for (int vi=verts.size()-1; vi>-1; vi=verts.get(vi)[2])
                            pts.add(Arrays.copyOfRange(verts.get(vi),0,2));
                        break BFS;
                    }
                }
            }
        }
        if (pts==null) {
            System.out.println(row+" "+clm+" "+tcol);
            for (int r=0; r<N; r++) {
                for (int c=0; c<N; c++)
                    System.out.print(grid[r][c]);
                System.out.println();
            }
        }
        for (int pi=0; pi<pts.size()-1; pi++) {
            int[] p0=pts.get(pi), p1=pts.get(pi+1);
            swap(grid,mvs,p0[0],p0[1],p1[0],p1[1]);
        }
        if (grid[row][clm]!=tcol) throw new RuntimeException();
        grid[row][clm]=C;
    }
    private String[] solve(int[][] start, int[][] end) {
        //IDEA FOR TRANSITION: greedily solve next topmost row or next bottommost row
        int[][] grid=new int[N][];
        for (int i=0; i<N; i++) grid[i]=Arrays.copyOf(start[i],N);
        List<String> mvs=new ArrayList<>();
        for (int row=0; row<N; row++) for (int clm=0; clm<N; clm++)
            solveLoc(grid,mvs,row,clm,end[row][clm]);
        return mvs.toArray(new String[0]);
    }
    public String[] solution(int N, int C, int[][] grid) {
        long st=System.currentTimeMillis();
        this.N=N; this.C=C;
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
        int[][] end=new int[N][N];
        for (int col=0, li=0; col<C; col++)
            for (int rep=0; rep<freq[col]; rep++, li++) {
                int l=locOrd[li];
                end[l/N][l%N]=col;
            }
        String[] sol=solve(grid,end);
        SplittableRandom rnd=new SplittableRandom(1);
        long STEPSZ=1000, MARK=0;
        List<List<List<int[]>>> touchers=new ArrayList<>();
        boolean eval_touchers=true;
        //touchers.get(a).get(b)=list of all cells of color a adjacent to a cell of color b in array end[][]
        for (long REPS=0, ACCN=0;; REPS++) {
            long TIME=System.currentTimeMillis()-st;
            if (TIME>=MARK||TIME>=TL) {
                System.out.printf("%8d%8d%8d%8d%n",TIME,REPS,ACCN,sol.length);
                MARK+=STEPSZ;
            }
            if (TIME>=TL) break;
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
            int[] pa=null, pb=null; int ca=-1, cb=-1; for (int tries=0; tries<100; tries++) {
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