import java.util.*;
import java.io.*;
public class B {
    public static void main(String[] args) throws IOException {
        long st=System.currentTimeMillis();
        int RL=1<<20;
        double[] RND_LOG=new double[RL];
        for (int i=0; i<RL; i++)
            RND_LOG[i]=Math.log((i+0.5)/RL);
        String f$="b4";
        System.out.println("f$="+f$);
        BufferedReader in=new BufferedReader(new FileReader(f$+".in"));
        StringTokenizer tok=new StringTokenizer(in.readLine());
        int N=Integer.parseInt(tok.nextToken()),
                M=Integer.parseInt(tok.nextToken());
        List<List<Integer>> help=new ArrayList<>();
        for (int i=0; i<N; i++)
            help.add(new ArrayList<>());
        for (int e=0; e<M; e++) {
            tok=new StringTokenizer(in.readLine());
            int a=Integer.parseInt(tok.nextToken()),
                    b=Integer.parseInt(tok.nextToken());
            a--; b--;
            help.get(a).add(b);
            help.get(b).add(a);
        }
        int[][] adjss=new int[N][];
        for (int i=0; i<N; i++) {
            adjss[i]=new int[help.get(i).size()];
            for (int j=0; j<adjss[i].length; j++)
                adjss[i][j]=help.get(i).get(j);
        }
        boolean[] chosen=new boolean[N];
        int scr=0;
        if (true) { //use previous solution
            in=new BufferedReader(new FileReader(f$+".out"));
            in.readLine();
            tok=new StringTokenizer(in.readLine());
            for (int i=0; i<N; i++)
                chosen[i]=Integer.parseInt(tok.nextToken())==1;
            for (boolean b:chosen) if (b) scr++;
        }
        int[] ncn=new int[N]; //ncn[i]=# neighbors of i that are chosen
        for (int i=0; i<N; i++)
            if (chosen[i]) {
                for (int n:adjss[i])
                    ncn[n]++;
            }
        SplittableRandom rnd=new SplittableRandom(1);
        long TIME=System.currentTimeMillis(), TL=200_000, END=TIME+TL,
                TLOG=2000,LOG=TIME+TLOG;
        long REPS=0, ACCN=0;
        System.out.printf("%15d%15d%6d%n",REPS,ACCN,scr);
        while (true) {
            TIME=System.currentTimeMillis();
            if (TIME>END)
                break;
            double alpha=(1.0*END-TIME)/TL;
            double temp=0.5*alpha*alpha;
            int vi;
            do {
                vi=rnd.nextInt(N);
            }
            while (chosen[vi]);
            if (1>=ncn[vi] ||
                    //rnd.nextDouble()<Math.exp((1-ncn[vi])/temp)
                    temp*RND_LOG[rnd.nextInt(RL)]<1-ncn[vi]
                    ) {
                scr+=1-ncn[vi];
                chosen[vi]=true;
                for (int n:adjss[vi]) {
                    ncn[n]++; //vert vi has been added
                    if (chosen[n]) {
                        chosen[n]=false;
                        for (int nn:adjss[n])
                            ncn[nn]--; //vert n has been removed
                    }
                }
                ACCN++;
            }
            REPS++;
            if (TIME>LOG) {
                LOG+=TLOG;
                System.out.printf("%15d%15d%6d%8.4f%n",REPS,ACCN,scr,temp);
            }
        }
        System.out.printf("%15d%15d%6d%n",REPS,ACCN,scr);
        PrintWriter out=new PrintWriter(new FileWriter(f$+".out"));
        int cnt=0;
        for (boolean b:chosen) if (b) cnt++;
        out.println(cnt);
        StringBuilder s=new StringBuilder();
        for (int i=0; i<N; i++)
            s.append(i>0?" ":"").append(chosen[i]?1:0);
        out.println(s);
        out.close();
        System.out.println("time="+(System.currentTimeMillis()-st));
    }
}