//https://codegolf.stackexchange.com/questions/50299/draw-an-image-as-a-voronoi-map
import java.util.*;
public class StainedGlass {
    public double[] sharpness;
    private int[] pixelToCenter;
    private HashMap<Integer,HashSet<Integer>> voronoi;
    private HashMap<Integer,int[][]> colorsOfCenter;
    private int bruteForceCnt;
    private static int colDiff(int col0, int col1) {
        return Math.abs((col0>>16)-(col1>>16))
                +Math.abs(((col0>>8)&0xff)-((col1>>8)&0xff))
                +Math.abs((col0&0xff)-(col1&0xff));
    }
    private static int[] merged(int[] a, int[] b) { //merge two sorted lists
        int ai=0, bi=0;
        int[] out=new int[a.length+b.length];
        for (int i=0; i<out.length; i++) {
            if (ai<a.length && (bi>=b.length || a[ai]<=b[bi])) {
                out[i]=a[ai];
                ai++;
            }
            else {
                out[i]=b[bi];
                bi++;
            }
        }
        return out;
    }
    private static int[] sorted(int[] a) { //counting sort
        int min=Integer.MAX_VALUE, max=Integer.MIN_VALUE;
        for (int e:a) {
            if (e<min) min=e;
            if (e>max) max=e;
        }
        int[] freq=new int[max-min+1];
        for (int e:a) freq[e-min]++;
        for (int i=1; i<freq.length; i++) freq[i]+=freq[i-1];
        int[] out=new int[a.length];
        for (int i=a.length-1; i>-1; i--) {
            int e=a[i];
            out[freq[e-min]-1]=e; //<- change
            freq[e-min]--;
        }
        return out;
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
    private void processVoronoi(int[] pixelToCenter, int W, int x0, int y0, int x1, int y1, HashSet<Integer> pts) {
        int[] corners={x0*W+y0,x1*W+y0,x1*W+y1,x0*W+y1};
        for (int co:corners) {
            if (!pts.contains(pixelToCenter[co])) {
                //brute-force
                int x=co/W, y=co%W;
                int bestC=-1, minSqDist=Integer.MAX_VALUE;
                for (int c:pts) {
                    int dx=x-c/W, dy=y-c%W;
                    int test=dx*dx+dy*dy;
                    if (test<minSqDist) {
                        minSqDist=test;
                        bestC=c;
                    }
                }
                pixelToCenter[co]=bestC;
                bruteForceCnt++;
            }
        }
        //find possible immediate fills
        boolean allEqual=true;
        int cell=pixelToCenter[corners[0]];
        for (int i=1; i<4 && allEqual; i++)
            if (pixelToCenter[corners[i]]!=cell)
                allEqual=false;
        if (allEqual)
            for (int x=x0; x<=x1; x++)
                for (int y=y0; y<=y1; y++)
                    pixelToCenter[x*W+y]=cell;
        else {
            //split into 4 rects, or 2 if degenerate
            int xsplit=(x0+x1)/2, ysplit=(y0+y1)/2;
            if (x1==x0) {
                if (y1>y0) {
                    processVoronoi(pixelToCenter,W,x0,y0,x1,ysplit,pts);
                    processVoronoi(pixelToCenter,W,x0,ysplit+1,x1,y1,pts);
                }
            }
            else if (y1==y0) {
                processVoronoi(pixelToCenter,W,x0,y0,xsplit,y1,pts);
                processVoronoi(pixelToCenter,W,xsplit+1,y0,x1,y1,pts);
            }
            else {
                //fill any possible edges
                for (int i=0; i<4; i++) {
                    int j=i==3?0:i+1;
                    int c=pixelToCenter[corners[i]];
                    if (c==pixelToCenter[corners[j]]) {
                        int xi=corners[i]/W, yi=corners[i]%W, xj=corners[j]/W, yj=corners[j]%W;
                        for (int x=Math.min(xi,xj); x<=Math.max(xi,xj); x++)
                            for (int y=Math.min(yi,yj); y<=Math.max(yi,yj); y++)
                                pixelToCenter[x*W+y]=c;
                    }
                }
                processVoronoi(pixelToCenter,W,x0,y0,xsplit,ysplit,pts);
                processVoronoi(pixelToCenter,W,xsplit+1,y0,x1,ysplit,pts);
                processVoronoi(pixelToCenter,W,x0,ysplit+1,xsplit,y1,pts);
                processVoronoi(pixelToCenter,W,xsplit+1,ysplit+1,x1,y1,pts);
            }
        }
    }
    public HashMap<Integer,HashSet<Integer>> getVoronoi() {
        return this.voronoi;
    }
    private int score(HashMap<Integer,HashSet<Integer>> voronoi, int[] pixelToCenter, HashMap<Integer,int[][]> colorsOfCenter, int W, int[] pixels, HashSet<Integer> pts) {
        for (int c:pts) voronoi.put(c,new HashSet<>());
        bruteForceCnt=0;
        processVoronoi(pixelToCenter,W,0,0,pixels.length/W-1,W-1,pts);
        for (int i=0; i<pixelToCenter.length; i++) {
            int c=pixelToCenter[i];
            if (!voronoi.keySet().contains(c))
                voronoi.put(c,new HashSet<>());
            voronoi.get(c).add(i);
        }
        int out=0;
        for (int c:pts) {
            int num=voronoi.get(c).size();
            if (num==0) continue;
            int[] R=new int[num], G=new int[num], B=new int[num];
            int id=0;
            for (int pixelid:voronoi.get(c)) {
                int pixel=pixels[pixelid];
                R[id]=(pixel >> 16);
                G[id] = (pixel >> 8) & 0xff;
                B[id] = pixel & 0xff;
                id++;
            }
            R=sorted(R);
            G=sorted(G);
            B=sorted(B);
            colorsOfCenter.put(c,new int[][] {R,G,B});
            int cost=0;
            for (int i=0; i<num-1-i; i++) {
                cost+=Math.abs(R[num-1-i]-R[i]);
                cost+=Math.abs(G[num-1-i]-G[i]);
                cost+=Math.abs(B[num-1-i]-B[i]);
            }
            out+=cost;
        }
        return out;
    }
    public int[] create(int H, int[] pixels, int N) {
        long TIME0 = System.currentTimeMillis();
        SplittableRandom rnd = new SplittableRandom();
        int W = pixels.length / H;
        System.out.println(H + "x" + W);
        //pixels[R*W+C] describes the color of pixel in row R and column C
        ArrayList<Integer> rects=new ArrayList<>();
        ArrayList<Double> variances=new ArrayList<>();
        rects.add(0);
        rects.add(0);
        rects.add(H-1);
        rects.add(W-1);
        int[] imgR=new int[pixels.length], imgG=new int[pixels.length], imgB=new int[pixels.length];
        for (int i=0; i<pixels.length; i++) {
            int p=pixels[i];
            imgR[i]=p>>16;
            imgG[i]=(p>>8)&0xff;
            imgB[i]=p&0xff;
        }
        double totVar=0;
        int imgMR=imgR[sortedOrd(imgR)[pixels.length/2]],
                imgMG=imgG[sortedOrd(imgG)[pixels.length/2]],
                imgMB=imgB[sortedOrd(imgB)[pixels.length/2]];
        for (int ir:imgR) totVar+=Math.abs(ir-imgMR);
        for (int ig:imgG) totVar+=Math.abs(ig-imgMG);
        for (int ib:imgB) totVar+=Math.abs(ib-imgMB);
        variances.add(totVar);
        System.out.println("totVar="+totVar);
        int CELLCOUNT=1;
        while (CELLCOUNT<N) {
            int bid=-1;
            double maxvar=-0.5;
            for (int ri=0; ri<variances.size(); ri++) {
                double test=variances.get(ri);
                if (test>maxvar) {
                    maxvar=test;
                    bid=ri;
                }
            }
            int x0=rects.get(4*bid),
                    y0=rects.get(4*bid+1),
                    x1=rects.get(4*bid+2),
                    y1=rects.get(4*bid+3);
            //split x-way
            int xsplit=(x0+x1)/2, ysplit=(y0+y1)/2;
            double lsplitvar=0, rsplitvar=0;
            if (x1==x0) {
                lsplitvar=Double.POSITIVE_INFINITY;
                rsplitvar=Double.POSITIVE_INFINITY;
            }
            else {
                int lsize=(xsplit-x0+1)*(y1-y0+1), rsize=(x1-xsplit)*(y1-y0+1);
                int[] r=new int[lsize], g=new int[lsize], b=new int[lsize];
                int id=0;
                for (int x=x0; x<=xsplit; x++) {
                    for (int y=y0; y<=y1; y++) {
                        int pi=x*W+y;
                        r[id]=imgR[pi];
                        g[id]=imgG[pi];
                        b[id]=imgB[pi];
                        id++;
                    }
                }
                r=sorted(r);
                g=sorted(g);
                b=sorted(b);
                int i=0, j=r.length-1;
                while (i<j) {
                    lsplitvar+=r[j]-r[i]+g[j]-g[i]+b[j]-b[i];
                    i++;
                    j--;
                }
                //right
                r=new int[rsize]; g=new int[rsize]; b=new int[rsize];
                id=0;
                for (int x=xsplit+1; x<=x1; x++) {
                    for (int y=y0; y<=y1; y++) {
                        int pi=x*W+y;
                        r[id]=imgR[pi];
                        g[id]=imgG[pi];
                        b[id]=imgB[pi];
                        id++;
                    }
                }
                r=sorted(r);
                g=sorted(g);
                b=sorted(b);
                i=0;
                j=r.length-1;
                while (i<j) {
                    rsplitvar+=r[j]-r[i]+g[j]-g[i]+b[j]-b[i];
                    i++;
                    j--;
                }
            }
            //split y-way
            double usplitvar=0, dsplitvar=0;
            if (y1==y0) {
                usplitvar=Double.POSITIVE_INFINITY;
                dsplitvar=Double.POSITIVE_INFINITY;
            }
            else {
                int lsize=(ysplit-y0+1)*(x1-x0+1), rsize=(y1-ysplit)*(x1-x0+1);
                int[] r=new int[lsize], g=new int[lsize], b=new int[lsize];
                int id=0;
                for (int x=x0; x<=x1; x++) {
                    for (int y=y0; y<=ysplit; y++) {
                        int pi=x*W+y;
                        r[id]=imgR[pi];
                        g[id]=imgG[pi];
                        b[id]=imgB[pi];
                        id++;
                    }
                }
                r=sorted(r);
                g=sorted(g);
                b=sorted(b);
                int i=0, j=r.length-1;
                while (i<j) {
                    dsplitvar+=r[j]-r[i]+g[j]-g[i]+b[j]-b[i];
                    i++;
                    j--;
                }
                //right
                r=new int[rsize]; g=new int[rsize]; b=new int[rsize];
                id=0;
                for (int x=x0; x<=x1; x++) {
                    for (int y=ysplit+1; y<=y1; y++) {
                        int pi=x*W+y;
                        r[id]=imgR[pi];
                        g[id]=imgG[pi];
                        b[id]=imgB[pi];
                        id++;
                    }
                }
                r=sorted(r);
                g=sorted(g);
                b=sorted(b);
                i=0;
                j=r.length-1;
                while (i<j) {
                    usplitvar+=r[j]-r[i]+g[j]-g[i]+b[j]-b[i];
                    i++;
                    j--;
                }
            }
            variances.set(bid,-1.0);
            if (lsplitvar+rsplitvar<usplitvar+dsplitvar) {
                rects.add(x0);
                rects.add(y0);
                rects.add(xsplit);
                rects.add(y1);
                variances.add(lsplitvar);
                rects.add(xsplit+1);
                rects.add(y0);
                rects.add(x1);
                rects.add(y1);
                variances.add(rsplitvar);
                CELLCOUNT++;
            }
            else if (usplitvar+dsplitvar<Double.POSITIVE_INFINITY) {
                rects.add(x0);
                rects.add(y0);
                rects.add(x1);
                rects.add(ysplit);
                variances.add(dsplitvar);
                rects.add(x0);
                rects.add(ysplit+1);
                rects.add(x1);
                rects.add(y1);
                variances.add(usplitvar);
                CELLCOUNT++;
            }
        }
        HashSet<Integer> pts=new HashSet<>();
        for (int i=0; i<variances.size(); i++) {
            if (variances.get(i)>=0)
                pts.add(
                        (rects.get(4*i)+rects.get(4*i+2))/2*W
                                +(rects.get(4*i+1)+rects.get(4*i+3))/2
                );
        }
        voronoi=new HashMap<>();
        pixelToCenter=new int[pixels.length];
        for (int i=0; i<pixelToCenter.length; i++) pixelToCenter[i]=-1;
        colorsOfCenter=new HashMap<>();
        int TOTALCOST = score(this.voronoi, this.pixelToCenter, this.colorsOfCenter, W, pixels, pts);
        int[] out = new int[3 * N];
        int out_id = 0;
        for (int c : pts) {
            out[out_id] = c / W;
            out[out_id + 1] = c % W;
            int pnum = colorsOfCenter.get(c)[0].length;
            out[out_id + 2] = 65536 * colorsOfCenter.get(c)[0][pnum / 2]
                    + 256 * colorsOfCenter.get(c)[1][pnum / 2]
                    + colorsOfCenter.get(c)[2][pnum / 2];
            out_id += 3;
        }
        System.out.println(System.currentTimeMillis() - TIME0+" ms elapsed");
        System.out.printf("%-15s%-11s%-15s%s%n","FINALSCORE","TOTALCOST","FINALCOLCNT","ms elapsed");
        //group cells by color
        ArrayList<HashSet<Integer>> groupCells = new ArrayList<>();
        ArrayList<Integer> groupCost = new ArrayList<>();
        ArrayList<int[][]> groupCols = new ArrayList<>();
        HashMap<Integer, Integer> colToId = new HashMap<>();
        double colCnt = 0;
        for (int i = 0; i < N; i++) { //group according to color
            int col = out[3 * i + 2];
            if (!colToId.keySet().contains(col)) {
                colToId.put(col, (int) colCnt);
                colCnt++;
                groupCells.add(new HashSet<>());
            }
            groupCells.get(colToId.get(col)).add(i);
        }
        for (int i = 0; i < groupCells.size(); i++) { //find total col diffs of ea. group
            int[][] colarr = new int[3][0];
            for (int cid : groupCells.get(i)) { //center index
                int cval = out[3 * cid] * W + out[3 * cid + 1]; //actual center
                //System.out.println("cval="+cval);
                for (int type = 0; type < 3; type++)
                    colarr[type] = merged(colarr[type], colorsOfCenter.get(cval)[type].clone());
            }
            //colarr[type] is sorted intensities of color (R if type=0; G if type=1; B else)
            int cost = 0;
            for (int type = 0; type < 3; type++)
                for (int j = 0; j < colarr[type].length - 1 - j; j++)
                    cost += colarr[type][colarr[type].length - 1 - j] - colarr[type][j];
            groupCost.add(cost);
            groupCols.add(colarr);
        }
        double FINALCOLCNT = groupCells.size();
        double FINALSCORE = TOTALCOST * (1 + FINALCOLCNT / N) * (1 + FINALCOLCNT / N);
        System.out.printf("%-15.4f%-11s%-15s%s%n",FINALSCORE,TOTALCOST,FINALCOLCNT,System.currentTimeMillis()-TIME0+"");
        for (int gi = 0; gi < groupCost.size(); gi++) {
            if (groupCells.get(gi).size() == 0) continue;
            int bgj = -1, bnewcost = -1, bnewtotalcost = 0;
            int[][] bcolarr = new int[3][0];
            for (int gj = 0; gj < gi; gj++) {
                if (groupCells.get(gj).size() == 0) continue;
                int oldcost = groupCost.get(gi) + groupCost.get(gj);
                int[][] colarr = new int[3][];
                int newcost = 0;
                for (int type = 0; type < 3; type++) {
                    colarr[type] = merged(groupCols.get(gi)[type], groupCols.get(gj)[type]);
                    for (int i = 0; i < colarr[type].length - 1 - i; i++)
                        newcost+=colarr[type][colarr[type].length-1-i] - colarr[type][i];
                }
                int NEWTOTALCOST=TOTALCOST + newcost - oldcost;
                double NEWSCR = NEWTOTALCOST * (1 + (FINALCOLCNT - 1) / N) * (1 + (FINALCOLCNT - 1) / N);
                if (NEWSCR < FINALSCORE) {
                    bnewtotalcost = NEWTOTALCOST;
                    FINALSCORE = NEWSCR;
                    bgj = gj;
                    bnewcost = newcost;
                    for (int type = 0; type < 3; type++)
                        bcolarr[type] = colarr[type].clone();
                }
            }
            if (bgj > -1) {
                for (int cellid : groupCells.get(bgj))
                    groupCells.get(gi).add(cellid);
                for (int type = 0; type < 3; type++)
                    groupCols.get(gi)[type] = bcolarr[type].clone();
                groupCost.set(gi, bnewcost);
                groupCells.get(bgj).clear();
                groupCols.set(bgj,new int[0][0]);
                FINALCOLCNT--;
                TOTALCOST = bnewtotalcost;
            }
        }
        System.out.printf("%-15.4f%-11s%-15s%s%n",FINALSCORE,TOTALCOST,FINALCOLCNT,System.currentTimeMillis()-TIME0+"");
        for (int gi=1; gi<groupCost.size(); gi++) {
            if (groupCells.get(gi).size()==0) continue;
            int colnum=groupCols.get(gi)[0].length;
            int bcol=65536*groupCols.get(gi)[0][colnum/2]
                    +256*groupCols.get(gi)[1][colnum/2]
                    +groupCols.get(gi)[2][colnum/2];
            for (int cellid:groupCells.get(gi)) {
                out[3*cellid+2]=bcol;
            }
        }
        HashMap<Integer,Integer> centerToCol=new HashMap<>();
        for (int ci=0; ci<N; ci++)
            centerToCol.put(out[3*ci]*W+out[3*ci+1],out[3*ci+2]);
        //WARNING: colorsOfCenter no longer updated
        //adjust ea. cell randomly
        final int p_totalcost=TOTALCOST;
        for (int ci=0; ci<N; ci++) {
            if (System.currentTimeMillis()-TIME0>18500) {
                System.out.println("only tried cells 0 to "+(ci-1));
                break;
            }
            int center=out[3*ci]*W+out[3*ci+1];
            //try some other points
            //do not change voronoi or pixelToCenter themselves until we are sure an improvement has been found
            int x0=Integer.MAX_VALUE, y0=Integer.MAX_VALUE, x1=Integer.MIN_VALUE, y1=Integer.MIN_VALUE;
            for (int i:voronoi.get(center)) {
                int x=i/W, y=i%W;
                if (x<x0) x0=x;
                if (x>x1) x1=x;
                if (y<y0) y0=y;
                if (y>y1) y1=y;
            }
            int[] pixelToCenterWithRemoval=pixelToCenter.clone();
            pts.remove(center);
            processVoronoi(pixelToCenterWithRemoval,W,x0,y0,x1,y1,pts);
            pts.add(center);
            ArrayList<Integer> neighbors=new ArrayList<>();
            for (int dx=-20; dx<=20; dx++)
                for (int dy=-20; dy<=20; dy++) {
                    if (dx==0 && dy==0) continue;
                    int nx=out[3*ci]+dx, ny=out[3*ci+1]+dy;
                    if (nx<0 || nx>H-1 || ny<0 || ny>W-1) continue;
                    int pt=nx*W+ny;
                    if (voronoi.get(center).contains(pt))
                        neighbors.add(pt);
                }
            for (int i=neighbors.size()-1; i>0; i--) {
                int j=rnd.nextInt(i+1);
                int tmp=neighbors.get(i);
                neighbors.set(i,neighbors.get(j));
                neighbors.set(j,tmp);
            }
            int bestc=-1, btotalcost=TOTALCOST;
            int[] bpixelToCenter=new int[0];
            for (int reps=0; reps<5 && reps<neighbors.size(); reps++) {
                int new_center=neighbors.get(reps);
                int[] new_pixelToCenter=pixelToCenterWithRemoval.clone();
                int new_cost=0;
                for (int i=0; i<new_pixelToCenter.length; i++) {
                    int currcenter=new_pixelToCenter[i];
                    int dx=currcenter/W-i/W, dy=currcenter%W-i%W;
                    int currdist=dx*dx+dy*dy;
                    dx=new_center/W-i/W;
                    dy=new_center%W-i%W;
                    int newdist=dx*dx+dy*dy;
                    if (newdist<currdist)
                        new_pixelToCenter[i] = new_center;
                    int c=new_pixelToCenter[i];
                    int col=(c==new_center?out[3*ci+2]:centerToCol.get(c));
                    int pixel=pixels[i];
                    new_cost+=colDiff(col,pixel);
                }
                if (new_cost<btotalcost) {
                    btotalcost=new_cost;
                    bestc=new_center;
                    bpixelToCenter=new_pixelToCenter.clone();
                }
            }
            if (bestc>-1) {
                pts.remove(center);
                pts.add(bestc);
                out[3*ci]=bestc/W;
                out[3*ci+1]=bestc%W;
                pixelToCenter=bpixelToCenter.clone();
                //derive voronoi and colorsOfCenter from pixelToCenter
                voronoi.clear();
                for (int i=0; i<pixelToCenter.length; i++) {
                    int c=pixelToCenter[i];
                    if (!voronoi.keySet().contains(c))
                        voronoi.put(c,new HashSet<>());
                    voronoi.get(c).add(i);
                }
                TOTALCOST=btotalcost;
                centerToCol.remove(center);
                centerToCol.put(bestc,out[3*ci+2]);
            }
        }
        FINALSCORE*=(double)TOTALCOST/p_totalcost;
        System.out.printf("%-15.4f%-11s%-15s%s%n",FINALSCORE,TOTALCOST,FINALCOLCNT,System.currentTimeMillis()-TIME0+"");
        return out;
    }
    private static HashSet<Integer> randCombo(int n, int k, SplittableRandom r) {
        //https://stackoverflow.com/questions/2394246/algorithm-to-select-a-single-random-combination-of-values
        //Robert Floyd's algorithm
        HashSet<Integer> out=new HashSet<>();
        for (int i=0; i<k; i++) {
            int elem=r.nextInt(n);
            if (out.contains(elem)) out.add(i+n-k);
            else out.add(elem);
        }
        return out;
    }
    public static void main(String[] args) {
        long seed=8662196172743455593L;//new Random().nextLong();
        System.out.println(seed);
        Test t=new Test(seed);
        StainedGlass P=new StainedGlass();
        int[] RET=P.create(t.H,t.pixels,t.N);
    }
    public static class Test {
        public int H, W, N, elemNum;
        public int[] pixels;
        private SplittableRandom rnd;
        public Test(int H, int[] img, int N) {
            this.N=N;
            this.H=H;
            this.W=img.length/H;
            pixels=img.clone();
        }
        public Test(long seed) {
            rnd=new SplittableRandom(seed);
            N=500;
            H=600;
            W=600;
            pixels=new int[H*W];
            elemNum=1+H*W/10000;//rnd.nextInt(N/100,N/2);
            for (int elem=0; elem<elemNum; elem++) {
                //add random ellipses
                int xc=rnd.nextInt(H),
                        yc=rnd.nextInt(W),
                        r=rnd.nextInt(255),
                        g=rnd.nextInt(255),
                        b=rnd.nextInt(255),
                        xl=rnd.nextInt(H/2)+1,
                        yl=rnd.nextInt(W/2)+1,
                        varprob=rnd.nextInt(11)+5;
                for (int dx=0; dx<=xl; dx++) {
                    //dx^2/xl^2+dy^2/yl^2=1
                    //dx^2yl^2+dy^2xl^2=xl^2yl^2
                    //dy=sqrt(xl^2-dx^2-)*yl/xl
                    int pdy=(int)(Math.sqrt(xl*xl-dx*dx)*yl/xl);
                    for (int dy=0; dy<=pdy; dy++) {
                        int[] pids={
                                (xc+dx)*W+yc+dy,
                                (xc-dx)*W+yc+dy,
                                (xc-dx)*W+yc-dy,
                                (xc+dx)*W+yc-dy
                        };
                        for (int id=0; id<4; id++) {
                            int pid=pids[id];
                            try {
                                if (!((dx==0 && (id==1 || id==3)) || (dy==0 && (id==1 || id==3)))) {
                                    int R = ((pixels[pid] >> 16) + r) / 2,
                                            G = (((pixels[pid] >> 8) & 0xff) + g) / 2,
                                            B = ((pixels[pid] & 0xff) + b) / 2;
                                    pixels[pid] = R * 65536 + G * 256 + B;
                                }
                            } catch (Exception e) {}
                            if (rnd.nextInt(varprob)==0) {
                                r=randShift(r,1,0,255);
                                g=randShift(g,1,0,255);
                                b=randShift(b,1,0,255);
                            }
                        }
                    }
                }
            }
            for (int i=0; i<pixels.length; i++) {
                int p=pixels[i];
                int r=p>>16, g=(p>>8)&0xff, b=p&0xff;
                pixels[i] =65536*randShift(r,5,0,255)
                        +256*randShift(g,5,0,255)
                        +randShift(b,5,0,255);
            }
        }
        private int randShift(int num, int shift, int lbnd, int hbnd) {
            int low=Math.max(lbnd,num-shift), high=Math.min(hbnd,num+shift);
            return rnd.nextInt(high-low+1)+low;
        }
    }
}
//disable color merging for now