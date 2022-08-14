import java.io.*;
import java.util.*;
public class Lossy2dCompression {
    private static final PrintStream OUT=System.out;
    private int[][][] grids;
    private int N, H, W, T;
    private double P;
    private double terror;
    private int[][] pos;
    private double[][] errors;
    private int MH, MW;
    private int[][] layers;
    private int[][][] items;
    private double scr() {
        return comp()*P+loss()*(1-P);
    }
    private double comp() {
        return (double)(H*W)/T;
    }
    private double loss() {
        return terror/(12.5*T);
    }
    private String[] sol() {
        String[] out=new String[H+N];
        for (int r=0; r<H; r++) {
            StringBuilder str=new StringBuilder();
            for (int c=0; c<W; c++) {
                int amt=layers[r][c];
                str.append((char) ('A' + (amt==0?0:items[r][c][amt/2])));
            }
            out[r]=str.toString();
        }
        for (int i=0; i<N; i++)
            out[i+H]=pos[i][0]+" "+pos[i][1];
        return out;
    }
    private void add(int r, int c, int i, int v) {
        layers[r][c]++;
        for (int j=layers[r][c]-1; j>i; j--)
            items[r][c][j]=items[r][c][j-1];
        items[r][c][i]=v;
        recalc(r,c);
    }
    private void remove(int r, int c, int i) {
        layers[r][c]--;
        for (int j=i; j<layers[r][c]; j++)
            items[r][c][j]=items[r][c][j+1];
        recalc(r,c);
    }
    private void recalc(int r, int c) {
        errors[r][c]=0;
        for (int i=0; i<layers[r][c]; i++)
            errors[r][c] +=Math.abs(items[r][c][layers[r][c]/2]-items[r][c][i]);
    }
    private void add(int gi, int pr, int pc) {
        if (pos[gi][0]!=-1 || pos[gi][1]!=-1)
            throw new RuntimeException("Rectangle "+gi+" already on "+pr+", "+pc);
        pos[gi][0]=pr;
        pos[gi][1]=pc;
        for (int r=0; r<grids[gi].length; r++)
            for (int c=0; c<grids[gi][r].length; c++) {
                int[] nums=items[pr+r][pc+c];
                int amt=layers[pr+r][pc+c];
                terror-=errors[pr+r][pc+c];
                int s=0;
                while (s<amt && nums[s]<grids[gi][r][c])
                    s++;
                add(pr+r,pc+c,s,grids[gi][r][c]);
                terror+=errors[pr+r][pc+c];
            }
        /*H=Math.max(H,pr+grids[gi].length);
        W=Math.max(W,pc+grids[gi][0].length);*/
    }
    private void remove(int gi) {
        int pr=pos[gi][0], pc=pos[gi][1];
        for (int r=0; r<grids[gi].length; r++)
            for (int c=0; c<grids[gi][r].length; c++) {
                int[] nums=items[pr+r][pc+c];
                int amt=layers[pr+r][pc+c];
                terror-=errors[pr+r][pc+c];
                int s=0;
                while (s<amt && nums[s]!=grids[gi][r][c])
                    s++;
                if (s>=amt)
                    throw new RuntimeException("Rectangle "+gi+" already not on "+pr+", "+pc);
                remove(pr+r,pc+c,s);
                terror+=errors[pr+r][pc+c];
            }
        pos[gi][0]=-1;
        pos[gi][1]=-1;
    }
    private void resize() {
        H=0;
        W=0;
        for (int i=0; i<N; i++)
            if (pos[i][0]!=-1 && pos[i][1]!=-1) {
                H=Math.max(H,pos[i][0]+grids[i].length);
                W=Math.max(W,pos[i][1]+grids[i][0].length);
            }
    }
    private void clear() {
        for (int gi=0; gi<N; gi++)
            Arrays.fill(pos[gi],-1);
        terror =0;
        H=0;
        W=0;
        for (int r=0; r<MH; r++)
            Arrays.fill(layers[r],0);
        errors=new double[MH][MW];
    }
    public String[] findSolution(double P, int N, String[][] gridstrs) {
        long ST=System.currentTimeMillis();
        long SEARCH_END=ST+8000;
        long END=ST+9500;
        this.P=P;
        this.N=N;
        T=0;
        grids=new int[N][][];
        for (int i=0; i<N; i++) {
            String[] gstr=gridstrs[i];
            grids[i]=new int[gstr.length][gstr[0].length()];
            for (int r=0; r<grids[i].length; r++)
                for (int c=0; c<grids[i][0].length; c++)
                    grids[i][r][c]=gstr[r].charAt(c)-'A';
            T+=grids[i].length*grids[i][0].length;
        }
        int minH=0, minW=0;
        for (int gi=0; gi<N; gi++) {
            minH=Math.max(minH,grids[gi].length);
            minW=Math.max(minW,grids[gi][0].length);
        }
        pos=new int[N][2];
        for (int gi=0; gi<N; gi++)
            Arrays.fill(pos[gi],-1);
        terror =0;
        H=0;
        W=0;
        MH=60;
        MW=60;
        layers=new int[MH][MW];
        items=new int[MH][MW][N];
        errors=new double[MH][MW];
        List<int[]> rects=new ArrayList<>();
        for (int HH=minH; HH<=MH; HH++)
            for (int WW=minW; WW<=MW; WW++)
                if (HH!=WW)
                    rects.add(new int[] {HH,WW});
        rects.sort(new Comparator<int[]>() {
            private int sz(int[] r) {
                return r[0]*r[1];
            }
            public int compare(int[] o1, int[] o2) {
                return sz(o1)-sz(o2);
            }
        });
        for (int s=Math.max(minH,minW); s<=60; s++)
            rects.add(s-Math.max(minH,minW),new int[] {s,s});
        Integer[] ord=new Integer[N];
        for (int gi=0; gi<N; gi++)
            ord[gi]=gi;
        Arrays.sort(ord, new Comparator<Integer>() {
            private int sz(int i) {
                return grids[i].length*grids[i][0].length;
            }
            public int compare(Integer o1, Integer o2) {
                return sz(o2)-sz(o1);
            }
        });
        String[] bsol=null;
        double bscr=Double.POSITIVE_INFINITY;
        String mes="no solution found";
        int bH=-1, bW=-1;
        for (int[] R:rects) {
            int HH=R[0], WW=R[1];
            //System.out.println(HH+" "+WW);
            if (System.currentTimeMillis()>SEARCH_END) {
                OUT.println("stop at area "+HH+"x"+WW+" ("+HH*WW+")");
                break;
            }
            if ((double)HH*WW/T*P>bscr)
                continue;
            boolean early=false;
            clear();
            for (int gi:ord) {
                if ((double)HH*WW/T*P+loss()*(1-P)>bscr) {
                    early=true;
                    break;
                }
                int br=-1, bc=-1;
                double berr=Double.POSITIVE_INFINITY;
                for (int r=0; r<=HH-grids[gi].length; r++)
                    for (int c=0; c<=WW-grids[gi][0].length; c++) {
                        add(gi,r,c);
                        double s=loss();
                        if (s<berr) {
                            berr=s;
                            br=r;
                            bc=c;
                        }
                        remove(gi);
                    }
                add(gi,br,bc);
            }
            resize();
            if (!early) {
                double scr = scr();
                if (scr < bscr) {
                    bscr = scr;
                    bsol = sol();
                    bH=HH;
                    bW=WW;
                    mes = "best: " + HH + "x" + WW + "->" + scr;
                }
            }
        }
        OUT.println(mes);
        clear();
        while (System.currentTimeMillis()<END) {
            for (int gi:ord) {
                if (System.currentTimeMillis()>END)
                    break;
                if (pos[gi][0]!=-1)
                    remove(gi);
                int br=-1, bc=-1;
                double berr=Double.POSITIVE_INFINITY;
                for (int r=0; r<=bH-grids[gi].length; r++)
                    for (int c=0; c<=bW-grids[gi][0].length; c++) {
                        add(gi,r,c);
                        double s=loss();
                        if (s<berr) {
                            berr=s;
                            br=r;
                            bc=c;
                        }
                        remove(gi);
                    }
                add(gi,br,bc);
            }
            resize();
            double scr = scr();
            if (scr < bscr) {
                bscr = scr;
                bsol = sol();
                mes = "improve ->" + scr+" ("+H+"x"+W+")";
            }
        }
        OUT.println(mes);
        return bsol;
    }
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            Double P = Double.parseDouble(br.readLine());
            int N = Integer.parseInt(br.readLine());
            String[][] grids=new String[N][];
            for (int i=0; i<N; i++) {
                int H = Integer.parseInt(br.readLine());
                grids[i] = new String[H];
                for (int k=0; k<H; k++)
                    grids[i][k] = br.readLine();
            }
            System.err.println(N);
            Lossy2dCompression prog = new Lossy2dCompression();
            String[] ret = prog.findSolution(P, N, grids);
            System.out.println(ret.length-N);
            for (int i = 0; i < ret.length; i++)
                System.out.println(ret[i]);
            System.out.flush();
        }
        catch (Exception e) {}
    }
}