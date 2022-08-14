import java.io.*;
import java.util.*;
public class LostTreasureHunter {
    private static final int MS=1000, TCNT=50;
    private static final double EA=0.175, AA=0.75;
    private static final int[] STOP={-1,0};
    private int V, C, N, T, S;
    private double E;
    private List<Integer> scrs;
    private SplittableRandom rnd;
    private int lastcnt, totremoved;
    private int maybenewcnt;
    private int pathnum;
    private double avgroom;
    private boolean backtracked, maybenew;
    private int[] ncnts;
    public void init(int treasureValue, int stepCost, int numChambers, int maxTreasure) {
        this.V = treasureValue;
        this.C = stepCost;
        this.N = numChambers;
        this.T = maxTreasure;
        this.S = 0;
        scrs=new ArrayList<>();
        rnd=new SplittableRandom(1);
        lastcnt=0;
        totremoved=0;
        avgroom=25;
        E=25;
        backtracked=false;
        pathnum=-1;
        ncnts=null;
        maybenew=true;
        maybenewcnt=0;
    }
    public int[] findSolution(int cnt, int deg, String time) {
        if (maybenew && rnd.nextDouble()<(N-maybenewcnt)/(double)N) {
            E+=(cnt-25.0)/N;
            maybenewcnt++;
        }
        int totscr=0;
        for (int i=0; i<scrs.size(); i++)
            totscr+=(i+1)*scrs.get(i);
        if (T*V<C || (totscr<0 && scrs.size()>=TCNT) || Integer.parseInt(time)>=9500 || S>=MS) return STOP;
        int amt=Math.min(cnt,T);
        int scr=amt*V-C;
        scrs.add(scr);
        if (scrs.size()>TCNT) scrs.remove(0);
        S++;
        if (maybenew) avgroom=(1-EA)*avgroom+EA*cnt;
        double expcnt=Math.max(1,(1-AA)*(E-totremoved/(double)N)+AA*avgroom);
        //System.out.println("estavg="+((1-AA)*(E-totremoved/(double)N)+AA*avgroom)+" "+(E-totremoved/(double)N));
        boolean goback;
        int next;
        if (backtracked) {
            goback=false;
            List<Integer> unknowns=new ArrayList<>();
            for (int i=0; i<deg; i++)
                if (ncnts[i]==-1) unknowns.add(i);
            int best=0;
            for (int i=0; i<deg; i++)
                if (ncnts[i]>ncnts[best])
                    best=i;
            if (ncnts[best]>=expcnt || unknowns.size()==0) {
                next=best;
                maybenew=false;
            }
            else {
                next=unknowns.get(rnd.nextInt(unknowns.size()));
                maybenew=true;
            }
        }
        else {
            goback=lastcnt>=expcnt;
            if (goback) {
                next=-1;
                ncnts[pathnum]=cnt-amt;
                maybenew=false;
            }
            else {
                next=rnd.nextInt(deg);
                ncnts=new int[deg];
                Arrays.fill(ncnts,-1);
                maybenew=true;
            }
        }
        //System.out.println((goback?"B":"-")+" "+(backtracked?"B":"-")+"\n"+deg+" "+Arrays.toString(ncnts));
        lastcnt=cnt-amt;
        totremoved+=amt;
        if (!goback) pathnum=next;
        backtracked=goback;
        return new int[] {amt,next};
    }
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            int treasureValue, stepCost, numChambers, maxTreasure;
            treasureValue = Integer.parseInt(reader.readLine());
            stepCost = Integer.parseInt(reader.readLine());
            numChambers = Integer.parseInt(reader.readLine());
            maxTreasure = Integer.parseInt(reader.readLine());
            LostTreasureHunter o = new LostTreasureHunter();
            o.init(treasureValue, stepCost, numChambers, maxTreasure);
            for (; ; ) {
                int treasureCount = Integer.parseInt(reader.readLine());
                int pathCount = Integer.parseInt(reader.readLine());
                String time = reader.readLine();

                int[] res = o.findSolution(treasureCount, pathCount, time);

                System.out.println(res[0]);
                if (res[0] > -1) {
                    System.out.println(res[1]);
                }
                System.out.flush();
            }
        } catch (Exception e) {}
    }
}