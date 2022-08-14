import java.security.*;
import java.util.*;
public class PointsOnGrid {
    private static final long SAMILLIS=6500;
    public int[] scores, errors;
    //SA: flip one cell as painted or unpainted
    private static void addbit(int[][] BIT,int amt,int x,int y) {
        int xi=x+1;
        while (xi<=BIT.length) {
            int yi=y+1;
            while (yi<=BIT[xi-1].length) {
                BIT[xi-1][yi-1]+=amt;
                yi+=yi&(-yi);
            }
            xi+=xi&(-xi);
        }
    }
    private static int prebit(int[][] BIT,int x,int y) {
        int xi=x+1;
        int out=0;
        while (xi>0) {
            int yi=y+1;
            while (yi>0) {
                out+=BIT[xi-1][yi-1];
                yi-=yi&(-yi);
            }
            xi-=xi&(-xi);
        }
        return out;
    }
    private static int subgridbit(int[][] BIT, int x0, int y0, int x1, int y1) {
        return prebit(BIT,x1,y1)-(x0>0?prebit(BIT,x0-1,y1):0)-(y0>0?prebit(BIT,x1,y0-1):0)+(x0>0 && y0>0?prebit(BIT,x0-1,y0-1):0);
    }
    private static int[] sortedOrd(int[] a) {
        //counting sort
        int min=Integer.MAX_VALUE, max=Integer.MIN_VALUE;
        for (int e:a) {
            if (e<min) min=e;
            if (e>max) max=e;
        }
        int[] freq=new int[max-min+1];
        for (int e:a) freq[e-min]++;
        for (int i=1; i<freq.length; i++) freq[i]+=freq[i-1];
        //freq[e-min]=# of ints in a <=e
        int[] out=new int[a.length];
        for (int i=a.length-1; i>-1; i--) {
            int e=a[i];
            out[freq[e-min]-1]=i;
            freq[e-min]--;
        }
        return out;
    }
    private static int[] sortedOrd(int[] a, int[] b) {
        //merge sort
        int[] out=new int[a.length], buf=new int[a.length];
        for (int i=0; i<out.length; i++)
            out[i]=i;
        for (int size=1; size<a.length; size*=2) {
            for (int i=0; i+size<a.length; i+=2*size) {
                int end=Math.min(a.length,i+2*size);
                int p0=i, p1=i+size;
                for (int bi=i; bi<end; bi++) {
                    if (p0<i+size &&
                            (p1>=end || a[out[p0]]<a[out[p1]] ||
                                    a[out[p0]]==a[out[p1]] && b[out[p0]]<=b[out[p1]])) {
                        buf[bi]=out[p0];
                        p0++;
                    }
                    else {
                        buf[bi]=out[p1];
                        p1++;
                    }
                }
                for (int bi=i; bi<end; bi++)
                    out[bi]=buf[bi];
            }
        }
        return out;
    }
    private static int subgridsum(int[][] prefix, int x0, int y0, int x1, int y1) {
        return prefix[x1][y1]-(x0>0?prefix[x0-1][y1]:0)-(y0>0?prefix[x1][y0-1]:0)+(x0>0 && y0>0?prefix[x0-1][y0-1]:0);
    }
    private boolean[][] bestPts(int H, int W, int h, int w, int Kmin, int Kmax, int[][] grid) {
        scores=new int[25000000];
        errors=new int[25000000];
        int TRACKERID=0;
        boolean[][] out=new boolean[H][W];
        int score=0;
        int error=0; //total amt below Kmin or aboe Kmax for ea. subgrid
        SplittableRandom rnd=new SplittableRandom();
        int[][] boardbit=new int[H][W];
        for (int i=0; i<H*W; i++) {
            out[i/W][i%W]=rnd.nextInt(h*w)<(Kmax+Kmin)/2;
            if (out[i/W][i%W]) {
                score+=grid[i/W][i%W];
                addbit(boardbit,1,i/W,i%W);
            }
        }
        final int SH=H-h+1, SW=W-w+1;
        /*
        int[] vals=new int[H*W];
        for (int i=0; i<H*W; i++)
            vals[i]=grid[i/W][i%W];
        int[] valord=sortedOrd(vals);
        for (int oi=H*W-1; oi>-1; oi--) {
            int rp=valord[oi]/W, cp=valord[oi]%W;
            int sgi0=Math.max(0,rp-h+1), sgi1=Math.min(SH-1,rp),
                    sgj0=Math.max(0,cp-w+1), sgj1=Math.min(SW-1,cp);
            boolean valid=true;
            for (int i=sgi0; i<=sgi1 && valid; i++)
                for (int j=sgj0; j<=sgj1 && valid; j++) {
                    int amt=subgridbit(boardbit,i,j,i+h-1,j+w-1);
                    if (amt>=Kmax)
                        valid=false;
                }
            if (valid) {
                out[rp][cp]=true;
                score+=grid[rp][cp];
                addbit(boardbit,1,rp,cp);
            }
        }
        */
        /*int[] timescontained=new int[H*W], vals=new int[H*W];
        for (int i=0; i<H*W; i++) {
            int rp=i/W, cp=i%W;
            int sgi0=Math.max(0,rp-h+1), sgi1=Math.min(SH-1,rp),
                    sgj0=Math.max(0,cp-w+1), sgj1=Math.min(SW-1,cp);
            vals[i]=-grid[rp][cp];
            timescontained[i]=(sgi1-sgi0+1)*(sgj1-sgj0+1);
        }
        int[] greedyord=sortedOrd(vals,timescontained);
        for (int oi=0; oi<H*W-1; oi++) {
            int rp=greedyord[oi]/W, cp=greedyord[oi]%W;
            int sgi0=Math.max(0,rp-h+1), sgi1=Math.min(SH-1,rp),
                    sgj0=Math.max(0,cp-w+1), sgj1=Math.min(SW-1,cp);
            boolean valid=true;
            for (int i=sgi0; i<=sgi1 && valid; i++)
                for (int j=sgj0; j<=sgj1 && valid; j++) {
                    int amt=subgridbit(boardbit,i,j,i+h-1,j+w-1);
                    if (amt>=Kmax)
                        valid=false;
                }
            if (valid) {
                out[rp][cp]=true;
                score+=grid[rp][cp];
                addbit(boardbit,1,rp,cp);
            }
        }*/
        int[][] subgridCnt=new int[SH][SW];
        for (int r=0; r<SH; r++)
            for (int c=0; c<SW; c++) {
                subgridCnt[r][c]=subgridbit(boardbit,r,c,r+h-1,c+w-1);
                error+=Math.max(0,Math.max(Kmin-subgridCnt[r][c] ,subgridCnt[r][c]-Kmax));
            }
        long TIMEOUT=System.currentTimeMillis()+SAMILLIS,
                TIMESWITCH=System.currentTimeMillis()+SAMILLIS/4,
                TIMELOG=System.currentTimeMillis()+SAMILLIS/10;
        double temp=5;
        double starttemp=20;
        int REPS=0, ACCEPTED=0;
        boolean PAIRMODE=false;
        System.out.printf("%8s%10s%8s%8s%12s%n","REPS","ACCEPTED","score","error","temp");
        /*int reallast=0;
        for (int i=SH-1; i<H; i++)
            for (int j=SW-1; j<W; j++)
                if (out[i][j])
                    reallast++;*/
        System.out.printf("%8d%10s%8d%8d%12.4f%n",REPS,ACCEPTED,score,error,temp/*,subgridCnt[SH-1][SW-1],reallast*/);
        final int PENALTY=10;
        while (true) {
            long TIME=System.currentTimeMillis();
            if (TIME>TIMEOUT)
                break;
            double fit=score-PENALTY*error;
            if (!PAIRMODE && TIME>=TIMESWITCH)
                PAIRMODE=true;
            if (!PAIRMODE) {
                int rsg=rnd.nextInt(SH*SW);
                int rsi=rsg/SW, rsj=rsg%SW;
                /*int rcell=rnd.nextInt(H*W);
                int rp=rcell/W,cp=rcell%W;*/
                int rp=rnd.nextInt(h)+rsi, cp=rnd.nextInt(w)+rsj;
                int nscore=score
                        +(out[rp][cp]?
                        (-grid[rp][cp]):
                        grid[rp][cp]);
                int nerror=error;
                int sgi0=Math.max(0,rp-h+1), sgi1=Math.min(SH-1,rp),
                        sgj0=Math.max(0,cp-w+1), sgj1=Math.min(SW-1,cp);
                for (int i=sgi0; i<=sgi1; i++)
                    for (int j=sgj0; j<=sgj1; j++)
                        if (out[rp][cp]) { //if removing pt
                            if (subgridCnt[i][j]<=Kmin)
                                nerror++;
                            else if (subgridCnt[i][j]>Kmax)
                                nerror--;
                        }
                        else { //if adding pt
                            if (subgridCnt[i][j]<Kmin)
                                nerror--;
                            else if (subgridCnt[i][j]>=Kmax)
                                nerror++;
                        }
                double nfit=nscore-PENALTY*nerror;
                if (nfit>fit || rnd.nextDouble()<Math.exp((nfit-fit)/temp)) {
                    for (int i=sgi0; i<=sgi1; i++)
                        for (int j=sgj0; j<=sgj1; j++)
                            if (out[rp][cp])
                                subgridCnt[i][j]--;
                            else
                                subgridCnt[i][j]++;
                    out[rp][cp]=!out[rp][cp];
                    score=nscore;
                    error=nerror;
                    ACCEPTED++;
                }
            }
            else {
                int[] rcs;
                if (TIME<TIMEOUT/*-SAMILLIS/4*/) {
                    rcs=new int[] {rnd.nextInt(H*W-1),rnd.nextInt(H*W)};
                    if (rcs[1]==rcs[0])
                        rcs[1]=H*W-1;
                }
                else {
                    rcs=new int[2];
                    if (rnd.nextInt(2)==0) {
                        rcs[0]=rnd.nextInt(H)*W+rnd.nextInt(W-1);
                        rcs[1]=rcs[0]+1;
                    }
                    else {
                        rcs[0]=rnd.nextInt(H-1)*W+rnd.nextInt(W);
                        rcs[1]=rcs[0]+W;
                    }
                }
                int[][] nsubgridCnt=new int[SH][];
                for (int i=0; i<SH; i++)
                    nsubgridCnt[i]=subgridCnt[i].clone();
                int nscore=score;
                int nerror=error;
                for (int rcell:rcs) {
                    int rp=rcell/W,cp=rcell%W;
                    nscore+=(out[rp][cp]?
                            (-grid[rp][cp]):
                            grid[rp][cp]);
                    int sgi0=Math.max(0,rp-h+1), sgi1=Math.min(SH-1,rp),
                            sgj0=Math.max(0,cp-w+1), sgj1=Math.min(SW-1,cp);
                    for (int i=sgi0; i<=sgi1; i++)
                        for (int j=sgj0; j<=sgj1; j++)
                            if (out[rp][cp]) { //if removing pt
                                if (nsubgridCnt[i][j]<=Kmin)
                                    nerror++;
                                else if (nsubgridCnt[i][j]>Kmax)
                                    nerror--;
                            }
                            else { //if adding pt
                                if (nsubgridCnt[i][j]<Kmin)
                                    nerror--;
                                else if (nsubgridCnt[i][j]>=Kmax)
                                    nerror++;
                            }
                    for (int i=sgi0; i<=sgi1; i++)
                        for (int j=sgj0; j<=sgj1; j++)
                            if (out[rp][cp])
                                nsubgridCnt[i][j]--;
                            else
                                nsubgridCnt[i][j]++;
                }
                double nfit=nscore-PENALTY*nerror;
                if (nfit>fit || rnd.nextDouble()<Math.exp((nfit-fit)/temp)) {
                    for (int i=0; i<SH; i++)
                        subgridCnt[i]=nsubgridCnt[i].clone();
                    for (int rcell:rcs) {
                        int rp=rcell/W,cp=rcell%W;
                        out[rp][cp]=!out[rp][cp];
                    }
                    score=nscore;
                    error=nerror;
                    ACCEPTED+=2;
                    /*
                    error-=Math.max(0,Math.max(subgridCnt[SH-1][SW-1]-Kmax,Kmin-subgridCnt[SH-1][SW-1]));
                    reallast=0;
                    for (int i=SH-1; i<H; i++)
                        for (int j=SW-1; j<W; j++)
                            if (out[i][j])
                                reallast++;
                    subgridCnt[SH-1][SW-1]=reallast;
                    error+=Math.max(0,Math.max(subgridCnt[SH-1][SW-1]-Kmax,Kmin-subgridCnt[SH-1][SW-1]));
                    */
                }
            }
            if (TIME>TIMELOG) {
                TIMELOG+=SAMILLIS/10;
                System.out.printf("%8d%10s%8d%8d%12.4f%n",REPS,ACCEPTED,score,error,temp);
            }
            REPS++;
            if ((REPS&8191)==0) //repeat every time REPS is mult of certain power of 2
                temp*=0.99;
            scores[TRACKERID]=score;
            errors[TRACKERID]=error;
            TRACKERID++;
        }
        System.out.printf("%8d%10s%8d%8d%12.4f%n",REPS,ACCEPTED,score,error,temp);
        for (int[] row:subgridCnt) {
            StringBuilder str=new StringBuilder();
            for (int cnt:row)
                str.append(cnt+" ");
            System.out.println(str);
        }
        return out;
    }
    public String[] findSolution(int H, int W, int h, int w, int Kmin, int Kmax, String[] grid) {
        int[][] nums=new int[H][W];
        for (int i=0; i<H; i++)
            for (int j=0; j<W; j++)
                nums[i][j]=(int)(grid[i].charAt(j)-'0');
        boolean[][] sol=bestPts(H,W,h,w,Kmin,Kmax,nums);
        String[] out=new String[H];
        for (int i=0; i<H; i++) {
            StringBuilder row=new StringBuilder();
            for (boolean c:sol[i])
                row.append(c?'x':'.');
            System.out.println(row);
            out[i]=row.toString();
        }
        return out;
    }
    public static void main(String[] args) {
        Test test=new Test(3);
        PointsOnGrid program=new PointsOnGrid();
        String[] ret=program.findSolution(
                test.H,
                test.W,
                test.P,
                test.Q,
                test.Kmin,
                test.Kmax,
                test.numbers
        );
        System.out.println("OFFICIAL SCORE: "+test.score(ret));
    }
    //--------taken from visualizer source code--------
    public static class Test {
        private static int minN = 5, maxN = 50;
        private static int minM = 2;
        private static int minK1 = 1, minK2 = 0;
        private static int minNum = 0, maxNum = 9;
        public int H, W, P, Q, Kmin, Kmax;
        String[] numbers;
        public Test(long seed) {
            try {
                SecureRandom r1 = SecureRandom.getInstance("SHA1PRNG");
                r1.setSeed(seed);
                H = r1.nextInt(maxN - minN + 1) + minN;
                W = r1.nextInt(maxN - minN + 1) + minN;
                P = r1.nextInt((H-1) - minM + 1) + minM;
                Q = r1.nextInt((W-1) - minM + 1) + minM;
                Kmax = r1.nextInt((P*Q-1) - minK1 + 1) + minK1;
                Kmin = r1.nextInt((Kmax-1) - minK2 + 1) + minK2;
                numbers=new String[H];
                for (int i=0; i<H; i++) {
                    numbers[i]="";
                    for (int k=0; k<W; k++)
                        numbers[i]+=(r1.nextInt(maxNum - minNum + 1) + minNum);
                }
                System.out.println("seed = "+seed);
                System.out.println("H = "+H+", W = "+W+", h = "+P+", w = "+Q+", Kmin = "+Kmin+", Kmax = "+Kmax);
                for (int i=0; i<H; i++)
                    System.out.println(numbers[i]);
            }
            catch (Exception e) {
                System.err.println("An exception occurred while generating test case.");
                System.exit(1);
            }
        }
        public double score(String[] grid) {
            if (grid == null) {
                System.err.println("Your return contained invalid number of elements.");
                return -1.0;
            }
            if (grid.length != H) {
                System.err.println("Your return did not contain " + H + " elements.");
                return -1.0;
            }
            for (int r=0; r<H; r++) {
                if (grid[r].length()!=W) {
                    System.err.println("Row "+r+" does not contain "+W+" elements.");
                    return -1.0;
                }
                for (int c=0; c<W; c++) {
                    char a=grid[r].charAt(c);
                    if (a!='.' && a!='x') {
                        System.err.println("Row "+r+" column "+c+" contains an illegal character");
                        return -1.0;
                    }
                }
            }
            //check that all subgrids do not have too many or too few pts
            boolean ILLEGAL=false;
            for (int r1=0; r1<H-(P-1); r1++) {
                for (int c1=0; c1<W-(Q-1); c1++) {
                    int count=0;
                    for (int r2=r1; r2<r1+P; r2++)
                        for (int c2=c1; c2<c1+Q; c2++)
                            if (grid[r2].charAt(c2)=='x') count++;
                    if (count>Kmax) {
                        System.err.println("Subgrid starting at ("+r1+","+c1+") contains too many painted cells ("+count+")");
                        ILLEGAL=true;
                    }
                    if (count<Kmin) {
                        System.err.println("Subgrid starting at ("+r1+","+c1+") contains not enough painted cells ("+count+")");
                        ILLEGAL=true;
                    }
                    System.out.print(count+" ");
                }
                System.out.println();
            }
            if (ILLEGAL) return -1;
            int total=0;
            for (int i = 0; i < H; ++i)
                for (int j = 0; j < W; ++j)
                    if (grid[i].charAt(j) == 'x')
                        total+=(int)(numbers[i].charAt(j)-'0');
            return total;
        }
    }
}
//sub4