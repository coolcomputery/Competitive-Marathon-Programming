//https://www.topcoder.com/challenges/30092483
import java.io.*;
import java.util.*;
import java.security.*;
public class LineUp {
    private int N,X;
    private int[] heights, arrgs;
    private static int sq(int n) {
        return n*n;
    }
    private int err(int id, int loc) {
        return sq(heights[id]-arrgs[loc]);
    }
    private int mvt(int[] locs, int ai, int id, int loc) {
       /*return total movement of person id from ai-1-th to ai+1-th arrgs
           if that person is at (ai,loc) (ai-th arrangement, loc-th position)*/
        int previ=(ai>0?locs[(ai-1)*N+id]:id),
                nexti=ai<X-1?locs[(ai+1)*N+id]:-1;
        return Math.abs(previ-loc)+(nexti!=-1?Math.abs(nexti-loc):0);
    }
    private int[] bestLineup(long TL) {
        long TIME=System.currentTimeMillis(),
                TOUT=TIME+TL, TLOG=TIME+TL/10;
        SplittableRandom rnd=new SplittableRandom(1);
       /*double[] LOG=new double[1<<16];
       for (int i=0; i<LOG.length; i++)
            //i+0.5 DUE TO WLEITE
           LOG[i]=Math.log((i+0.5)/LOG.length);*/
        int[] out=new int[X*N];
        //out[a*N+i]=person at i-th place in a-th arrangement
        for (int i=0; i<X*N; i++)
            out[i]=i%N;
        int[] locs=new int[X*N];
        //locs[a*N+i]=location of person i at a-th arrangement
        for (int i=0; i<X*N; i++)
            locs[i/N*N+out[i]]=i%N;
        double T=2, temp=T;
        StringBuilder log=new StringBuilder();
        int mvt=0, sqerr=0;
        for (int i=0; i<X*N; i++) {
            int id=out[i];
            int ai=i/N;
            mvt+=Math.abs((ai>0?locs[(ai-1)*N+id]:id)-i%N);
            sqerr+=sq(heights[id]-arrgs[i]);
        }
        double scr=mvt+Math.sqrt(sqerr);
        int[] BEST=out.clone();
        double BSCR=scr;
        int BMVT=mvt, BSQERR=sqerr;
        int REPS=0, ACCN=0;
        String form="%9d%8d%8.2f%8d%12d%12.2f%n";
        int S=9;
        log.append("S="+S+"\n");
        log.append(String.format(form,REPS,ACCN,temp,mvt,sqerr,scr));
        ArrayList<int[]> pairs=new ArrayList<>();
        for (int i=1; i<N; i++)
            for (int j=Math.max(0,i-S); j<i; j++)
                pairs.add(new int[] {i,j});
        while (true) {
            TIME=System.currentTimeMillis();
            if (TIME>TOUT)
                break;
            double alpha=(double)(TOUT-TIME)/TL;
            //temp=T*Math.sqrt(alpha);
            temp=T*alpha;
            int ai=rnd.nextInt(X);
            int mut=alpha<0.5?0:1;
            /*int pi=rnd.nextInt(N);
            int pjcenter=Math.min(Math.max(pi,S),N-2-(S-1));
            int pj=rnd.nextInt(Math.max(0,pjcenter-S),Math.min(N-2,pjcenter+S-1));
            if (pj>=pi) pj++;*/
            int[] pair=pairs.get(rnd.nextInt(pairs.size()));
            int idi=out[ai*N+pair[0]], idj=out[ai*N+pair[1]];
            int nmvt=mvt;
            int nsqerr=sqerr;
            int lasti=ai==0?idi:locs[(ai-1)*N+idi],
                    lastj=ai==0?idj:locs[(ai-1)*N+idj];
            for (int a=ai; a<(mut==0?(ai+1):X); a++) {
                int pi=locs[a*N+idi], pj=locs[a*N+idj];
                int ii=a*N+pi, ij= a*N+pj;
                nsqerr-=err(idi,ii);
                nsqerr+=err(idi,ij);
                nsqerr-=err(idj,ij);
                nsqerr+=err(idj,ii);
            }
            if (mut==0) {
                int pi=pair[0], pj=pair[1];
                nmvt-=mvt(locs,ai,idi,pi);
                nmvt+=mvt(locs,ai,idi,pj);
                nmvt-=mvt(locs,ai,idj,pj);
                nmvt+=mvt(locs,ai,idj,pi);
            }
            else {
                int pi=locs[ai*N+idi], pj=locs[ai*N+idj];

                nmvt+=Math.abs(pj-lasti)-Math.abs(pi-lasti);
                nmvt+=Math.abs(pi-lastj)-Math.abs(pj-lastj);
            }
            double nscr=nmvt+Math.sqrt(nsqerr);
            if (nscr<scr ||
                    //(alpha<0.1?
                    Math.log(rnd.nextDouble())*temp
                            //:LOG[rnd.nextInt(LOG.length)]*temp)
                            <scr-nscr
                    ) {
                scr=nscr;
                mvt=nmvt;
                sqerr=nsqerr;
                //swap
                for (int a=ai; a<(mut==0?(ai+1):X); a++) {
                    int pi=locs[a*N+idi],
                            pj=locs[a*N+idj];
                    int ii=a*N+pi, ij=a*N+pj;
                    locs[a*N+idi]=pj;
                    locs[a*N+idj]=pi;
                    out[ii]=idj;
                    out[ij]=idi;
                }
                ACCN++;
                if (scr<BSCR) {
                    BSCR=scr;
                    BMVT=mvt;
                    BSQERR=sqerr;
                    BEST=out.clone();
                }

            }
            REPS++;
            if (TIME>TLOG) {
                TLOG+=TL/10;
                log.append(String.format(form,REPS,ACCN,temp,mvt,sqerr,scr));
            }
        }
        log.append(String.format(form,REPS,ACCN,temp,mvt,sqerr,scr));
        log.append("scr->"+BSCR+" ("+BMVT+", "+BSQERR+")\n");
        System.err.println(log);
        return BEST;
    }
    public int[] getLineup(int[] heights, int[] arrangements) {
        N=heights.length;
        X=arrangements.length/N;
        this.heights=heights.clone();
        arrgs=arrangements.clone();
        return bestLineup(9400);
    }
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            int[] heights = new int[Integer.parseInt(br.readLine())];
            for (int i = 0; i < heights.length; ++i) {
                heights[i] = Integer.parseInt(br.readLine());
            }
            int[] arrangements = new int[Integer.parseInt(br.readLine())];
            for (int i = 0; i < arrangements.length; ++i) {
                arrangements[i] = Integer.parseInt(br.readLine());
            }
            LineUp sol = new LineUp();
            int[] ret = sol.getLineup(heights, arrangements);
            System.out.println(ret.length);
            for (int i = 0; i < ret.length; i++) {
                System.out.println(ret[i]);
            }
            System.out.flush();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}