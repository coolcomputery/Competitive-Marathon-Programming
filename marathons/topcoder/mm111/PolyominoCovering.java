import org.omg.CORBA.TIMEOUT;

import java.io.*;
import java.util.*;
public class PolyominoCovering {
    //lookup table of free polyominoes
    //Source: Wikipedia
    //[width] binary code
    private long END;
    private StringBuilder log;
    private int[] PCSCR;
    private ArrayList<int[][]> POLYS;
    private ArrayList<Integer> POLYSZS;
    private int[][] GRID;
    private int[] LIMS;
    private int N;
    private static final int PENALTY=-1000;
    private static final String[] POLYSTRS={
            //do
            "1 11",
            //tri
            "1 111",
            "2 1011",
            //tetr
            "1 1111",
            "2 1111",
            "2 101011",
            "2 101101",
            "3 111010",
            //pent
            "1 11111",
            "3 011 110 010",
            "2 01 01 01 11",
            "2 11 11 01",
            "2 01 01 11 10",
            "3 111 010 010",
            "3 101 111",
            "3 001 001 111",
            "3 001 011 110",
            "3 010 111 010",
            "2 01 11 01 01",
            "3 011 010 110",
            //hex
            "1 111111",
            "2 11 10 10 10 10",
            "2 10 11 10 10 10",
            "2 10 10 11 10 10",
            "2 01 11 10 10 10",
            "2 11 11 10 10",
            "2 11 10 11 10",
            "2 11 10 10 11",
            "2 10 11 11 10",
            "3 111 100 100 100",
            "3 100 111 100 100",
            "3 111 010 010 010",
            "3 011 110 010 010",
            "3 011 010 110 010",
            "3 011 010 010 110",
            "3 010 011 110 010",
            "3 010 111 010 010",
            "3 010 111 100 100",
            "2 01 11 10 11",
            "2 01 01 11 10 10",
            "2 01 11 11 10",
            "2 11 11 11",
            "3 001 111 010 010",
            "3 111 011 010",
            "3 001 011 110 010",
            "3 001 111 100 100",
            "3 011 110 100 100",
            "3 111 101 100",
            "3 101 111 100",
            "3 101 111 010",
            "3 011 010 110 100",
            "3 100 110 111",
            "3 010 111 110",
            "3 001 111 110",
            "3 001 011 110 100",
            //hept
            "1 1111111",
            "2 11 10 10 10 10 10",
            "2 10 11 10 10 10 10",
            "2 10 10 11 10 10 10",
            "3 111 100 100 100 100",
            "3 100 111 100 100 100",
            "3 100 100 111 100 100",
            "2 11 11 10 10 10",
            "2 11 10 11 10 10",
            "2 11 10 10 11 10",
            "2 11 10 10 10 11",
            "2 10 11 11 10 10",
            "2 10 11 10 11 10",
            "4 1111 1000 1000 1000",
            "4 1000 1111 1000 1000",
            "3 111 110 100 100",
            "3 111 100 110 100",
            "3 111 100 100 110",
            "3 110 111 100 100",
            "3 110 100 111 100",
            "3 100 111 110 100",
            "2 11 11 11 10",
            "2 11 11 10 11",
            "3 111 101 100 100",
            "3 100 111 101 100",
            "3 001 111 100 100 100",
            "3 101 111 100 100",
            "3 010 111 100 100 100",
            "2 01 11 10 10 10 10",
            "2 01 01 11 10 10 10",
            "2 01 11 11 10 10",
            "3 011 110 100 100 100",
            "3 111 010 010 010 010",
            "3 011 110 010 010 010",
            "3 011 010 110 010 010",
            "3 011 010 010 110 010",
            //4th row
            "3 011 010 010 010 110",
            "3 010 111 010 010 010",
            "3 010 011 110 010 010",
            "3 010 011 010 110 010",
            "3 010 010 111 010 010",
            "4 0111 1100 0100 0100",
            "4 0111 0100 1100 0100",
            "4 0111 0100 0100 1100",
            "4 1100 0111 0100 0100",
            "4 0100 1111 0100 0100",
            "4 0100 0111 1100 0100",
            "4 0100 0111 0100 1100",
            //5th
            "3 111 011 010 010",
            "3 011 111 010 010",
            "3 011 011 110 010",
            "3 011 011 010 110",
            "3 110 011 011 010",
            "3 010 111 011 010",
            "3 111 010 011 010",
            "3 011 110 011 010",
            "3 011 010 111 010",
            "3 011 010 011 110",
            "3 111 010 010 011",
            "3 011 110 010 011",
            //6th
            "3 001 111 010 010 010",
            "3 001 011 110 010 010",
            "3 001 011 010 110 010",
            "3 001 011 010 010 110",
            "2 01 11 10 11 10",
            "2 01 11 10 10 11",
            "3 001 001 111 100 100",
            "3 001 111 101 100",
            "3 111 101 101",
            "3 111 101 110",
            "3 010 010 111 100 100",
            "3 010 111 110 100",
            //7th (unchecked)
            "3 011 110 110 100",
            "3 010 110 111 100",
            "3 111 110 110",
            "3 110 111 110",
            "3 101 111 010 010",
            "3 001 111 110 010",
            "3 001 111 110 100",
            "3 011 010 110 100 100",
            "3 010 011 110 100 100",
            "3 010 110 100 111",
            "4 0011 1110 1000 1000",
            "3 001 011 010 110 100",
            //8th
            "3 111 010 110 100",
            "3 110 011 110 100",
            "3 110 010 111 100",
            "4 0011 0110 1100 1000",
            "4 0111 1100 1000 1000",
            "4 0011 0010 1110 1000",
            "3 001 011 110 100 100",
            "4 0001 0011 1110 1000",
            "2 01 01 11 10 11",
            "3 011 110 100 110",
            "3 010 111 100 110",
            "4 0010 0011 1110 0100",
            //9th
            "4 0010 0011 0110 1100",
            "4 0010 0010 0111 1100",
            "3 101 111 110",
            "3 011 111 110",
            "3 001 001 111 110",
            "3 001 011 110 110",
            "3 101 111 101",
            "3 001 111 100 110",
            "4 0001 0111 1100 0100",
            "2 10 11 01 11 10",
            "3 010 111 101 100",
            "3 100 111 010 011"
    };
    private int[][] parsePoly(String str) {
        int split=str.indexOf(" ");
        int width=Integer.parseInt(str.substring(0,split));
        String info=str.substring(split+1);
        ArrayList<ArrayList<Integer>> poly=new ArrayList<>();
        for (int i=0; i<info.length(); i++) {
            if (poly.size()==0 || poly.get(poly.size()-1).size()==width)
                poly.add(new ArrayList<>());
            char pc=info.charAt(i);
            if (pc!='0' && pc!='1')
                continue;
            poly.get(poly.size()-1).add(Integer.parseInt(pc+""));
        }
        int[][] arr=new int[poly.size()][width];
        for (int i=0; i<poly.size(); i++)
            for (int j=0; j<poly.get(i).size(); j++)
                arr[i][j]=poly.get(i).get(j);
        return arr;
    }
    private int[][] rot(int[][] arr) {
        int h=arr.length, w=arr[0].length;
        int[][] out=new int[w][h];
        for (int i=0; i<h; i++)
            for (int j=0; j<w; j++)
                out[j][h-1-i]=arr[i][j];
        return out;
    }
    private int[][] flip(int[][] arr) {
        int h=arr.length, w=arr[0].length;
        int[][] out=new int[h][w];
        for (int i=0; i<h; i++)
            for (int j=0; j<w; j++)
                out[i][w-1-j]=arr[i][j];
        return out;
    }
    private int[][] clone(int[][] arr) {
        int[][] out=new int[arr.length][];
        for (int i=0; i<arr.length; i++)
            out[i]=arr[i].clone();
        return out;
    }
    private boolean eq(int[][] a, int[][] b) {
        if (a.length!=b.length || a[0].length!=b[0].length)
            return false;
        for (int i=0; i<a.length; i++)
            for (int j=0; j<a[i].length; j++)
                if (a[i][j]!=b[i][j])
                    return false;
        return true;
    }
    private ArrayList<int[][]> uniques(int[][] poly) {
        ArrayList<int[][]> made=new ArrayList<>();
        for (int r=0; r<4; r++) {
            int[][] ret=clone(poly);
            for (int rep=0; rep<r; rep++)
                ret=rot(ret);
            made.add(ret);
        }
        for (int i=0; i<4; i++)
            made.add(flip(made.get(i)));
        ArrayList<int[][]> uniques=new ArrayList<>();
        for (int[][] p:made) {
            boolean good=true;
            for (int[][] q:uniques)
                if (eq(p,q)) {
                    good=false;
                    break;
                }
            if (good)
                uniques.add(p);
        }
        return uniques;
    }
    private int size(int[][] pc) {
        int out=0;
        for (int i=0; i<pc.length; i++)
            for (int j=0; j<pc[0].length; j++)
                if (pc[i][j]==1)
                    out++;
        return out;
    }
    class Sol {
        int[] covering, cnts;
        int scr;
        public Sol() {
            covering=new int[N*N];
            for (int i=0; i<N*N; i++)
                covering[i]=-1;
            cnts=new int[LIMS.length];
            scr=N*N*PENALTY;
        }
        public Sol(int[] c, int[] cnts, int s) {
            covering=c.clone();
            this.cnts=cnts.clone();
            scr=s;
        }
        public Sol clone() {
            Sol out=new Sol();
            out.covering=covering.clone();
            out.scr=scr;
            out.cnts=cnts.clone();
            return out;
        }
        boolean valid(int mi) {
            int pcloc=mi%(N*N);
            int pi=mi/(N*N);
            int[][] pc=POLYS.get(pi);
            int h = pc.length, w=pc[0].length;
            boolean good=true;
            for (int i=0; i<h && good; i++)
                for (int j=0; j<w && good; j++)
                    if (pc[i][j]==1) {
                        int loc = (pcloc/N+i)*N+(pcloc%N+j);
                        if (covering[loc] != -1)
                            good=false;
                    }
            if (!good)
                return false;
            int sz=POLYSZS.get(pi);
            return cnts[sz-2]<LIMS[sz-2];
        }
        void add(int mi) {
            int pcloc=mi%(N*N);
            int[][] pc=POLYS.get(mi/(N*N));
            int h = pc.length, w=pc[0].length;
            int sz=POLYSZS.get(mi/(N*N));
            cnts[sz-2]++;
            for (int i=0; i<h; i++)
                for (int j=0; j<w; j++)
                    if (pc[i][j]==1)
                        covering[(pcloc/N+i)*N+(pcloc%N+j)]=mi;
            scr+=PCSCR[mi];
        }
        void remove(int mi) {
            int pcloc=mi%(N*N);
            int[][] pc=POLYS.get(mi/(N*N));
            int h = pc.length, w=pc[0].length;
            int sz=POLYSZS.get(mi/(N*N));
            cnts[sz-2]--;
            for (int i=0; i<h; i++)
                for (int j=0; j<w; j++)
                    if (pc[i][j]==1)
                        covering[(pcloc/N+i)*N+(pcloc%N+j)]=-1;
            scr-=PCSCR[mi];
        }
    }
    public int[] findSolution(int N, int[] grid, int[] tiles) {
        END=System.currentTimeMillis()+9700;
        log=new StringBuilder();
        this.N=N;
        GRID=new int[N][N];
        for (int i=0; i<N*N; i++)
            GRID[i/N][i%N]=grid[i];
        LIMS=tiles.clone();
        POLYS=new ArrayList<>(); //contains all free polys + rotations, reflections
        for (String str:POLYSTRS)
            POLYS.addAll(uniques(parsePoly(str)));
        POLYSZS=new ArrayList<>();
        for (int[][] pc:POLYS)
            POLYSZS.add(size(pc));
        PCSCR=new int[POLYS.size()*N*N];
        for (int i=0; i<PCSCR.length; i++)
            PCSCR[i]=-1;
        for (int pi=0; pi<POLYS.size(); pi++) {
            int[][] pc=POLYS.get(pi);
            int h=pc.length, w=pc[0].length;
            for (int i=0; i<N-h+1; i++) {
                for (int j=0; j<N-w+1; j++) {
                    int scr=1;
                    for (int r=0; r<h; r++)
                        for (int c=0; c<w; c++)
                            if (pc[r][c]==1)
                                scr*=GRID[i+r][j+c];
                    PCSCR[pi*N*N+(i*N+j)]=scr-PENALTY*POLYSZS.get(pi);
                }
            }
        }
        class Pair implements Comparable<Pair> {
            int id, scr;
            public Pair(int id, int scr) {
                this.id=id;
                this.scr=scr;
            }
            public int compareTo(Pair o) {
                return scr-o.scr;
            }
        }
        Pair[] PS=new Pair[PCSCR.length];
        for (int i=0; i<PCSCR.length; i++)
            PS[i]=new Pair(i,PCSCR[i]);
        Arrays.sort(PS);
        ArrayList<Integer> best_ids=new ArrayList<>();
        for (int ppi=PS.length-1; ppi>-1 && PS[ppi].scr>0; ppi--)
            best_ids.add(PS[ppi].id);
        Sol sol=new Sol();
        //TODO: put random piece & clear interfering pieces, then greedy fill
        ArrayList<Integer> rejected=new ArrayList<>();
        for (int pi:best_ids) {
            if (sol.valid(pi))
                sol.add(pi);
            else
                rejected.add(pi);
        }
        log.append(rejected.size()).append(" rejected pieces\n");
        int REPS=0;
        String form="%4d%10d";
        log.append(Arrays.toString(sol.covering)).append("\n");
        log.append(String.format(form,REPS,sol.scr)).append("\n");
        //log.append(Arrays.toString(sol.covering)).append("\n");
        int span=5;
        for (int ri:rejected) {
            long TIME=System.currentTimeMillis();
            if (TIME>END)
                break;
            Sol nsol=sol.clone();
            int pcloc=ri%(N*N);
            int sz=POLYSZS.get(ri/(N*N));
            int rloc=pcloc/N, cloc=pcloc%N;
            int[][] pc=POLYS.get(ri/(N*N));
            int h=pc.length, w=pc[0].length;
            for (int r=Math.max(0,rloc-span); r<Math.min(N,rloc+h+span); r++)
                for (int c=Math.max(0,cloc-span); c<Math.min(N,cloc+w+span); c++) {
                    int pi = nsol.covering[r*N+c];
                    if (pi != -1)
                        nsol.remove(pi);
                }
            if (nsol.cnts[sz-2]==LIMS[sz-2]) {
                //remove any other pieces so that size limit is not violated
                HashSet<Integer> pieces=new HashSet<>();
                for (int i:nsol.covering)
                    pieces.add(i);
                int bscr=Integer.MAX_VALUE;
                int bpi=-1;
                for (int i:pieces) {
                    if (POLYSZS.get(i/(N*N))!=sz)
                        continue;
                    int scr=PCSCR[i];
                    if (scr<bscr) {
                        bscr=scr;
                        bpi=i;
                    }
                }
                if (bpi==-1)
                    continue;
                nsol.remove(bpi);
            }
            nsol.add(ri);
            for (int pi:best_ids) {
                TIME=System.currentTimeMillis();
                if (TIME>END)
                    break;
                if (nsol.valid(pi)) {
                    nsol.add(pi);
                }
            }
            REPS++;
            if (nsol.scr>sol.scr) {
                sol = nsol;
                log.append(String.format(form,REPS,sol.scr)).append(" (sz="+sz+")").append("\n");
            }
        }
        log.append("reps=").append(REPS).append("\n");
        log.append(Arrays.toString(sol.covering)).append("\n");
        //log.append(Arrays.toString(sol.covering)).append("\n");
        System.out.print(log);
        int[] out=sol.covering;
        for (int i=0; i<N*N; i++)
            if (out[i]!=-1)
                out[i]++; //ID 0 is illegal
        return out;
    }
    // -------8<------- end of solution submitted to the website -------8<-------
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            int N = Integer.parseInt(br.readLine());
            int[] grid = new int[N*N];
            for (int i=0; i<N*N; i++) grid[i]=Integer.parseInt(br.readLine());
            int[] tiles = new int[6];
            for (int i=0; i<6; i++) tiles[i]=Integer.parseInt(br.readLine());

            PolyominoCovering pc = new PolyominoCovering();
            int[] ret = pc.findSolution(N, grid, tiles);

            System.out.println(ret.length);
            for (int i = 0; i < ret.length; ++i) {
                System.out.println(ret[i]);
            }
        }
        catch (Exception e) {}
    }
}