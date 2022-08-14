import java.util.*;
import java.io.*;
public class ContestOrganizer {
    private int[][] winmat;
    private int N, contestNum;
    private double f(double x) {
        return Math.min(0.95,Math.max(0.05,Math.pow(1-Math.pow(Math.abs(x-1),3.5),1.0/3.5)));
    }
    private void sortRanksum(ArrayList<Integer> ppl) {
        double[] wins=new double[N];
        for (int i=0; i<N; i++)
            wins[i]=1;
        for (int p:ppl)
            for (int j:ppl)
                if (p!=j)
                    wins[p]*=f((double)winmat[p][j]/contestNum);
        ppl.sort(new Comparator<Integer>() {
            public int compare(Integer a, Integer b) {
                return (int)Math.signum(wins[b]-wins[a]);
            }
        });
    }
    public String[] makeTeams(int M, String[] winarg) {
        StringBuilder log=new StringBuilder();
        N = winarg.length;
        winmat=new int[N][N];
        for (int i=0; i<N; i++) {
            String[] info=winarg[i].split(" ");
            for (int j=0; j<N; j++)
                winmat[i][j]=Integer.parseInt(info[j]);
        }
        contestNum=winmat[0][1]+winmat[1][0];
        ArrayList<ArrayList<Integer>> teams=new ArrayList<>();
        for (int i=0; i<M; i++)
            teams.add(new ArrayList<>());
        ArrayList<ArrayList<Integer>> groups=new ArrayList<>();
        groups.add(new ArrayList<>());
        for (int i=0; i<N; i++)
            groups.get(0).add(i);
        sortRanksum(groups.get(0));
        while (true) {
            for (ArrayList<Integer> g:groups)
                log.append(g);
            log.append("\n");
            ArrayList<ArrayList<Integer>> ngroups=new ArrayList<>();
            boolean good=false;
            for (ArrayList<Integer> g:groups) {
                if (g.size()<=2) {
                    ngroups.add(g);
                    continue;
                }
                good=true;
                int split=g.size()/2;
                ArrayList<Integer> l=new ArrayList<>(), r=new ArrayList<>();
                for (int i=0; i<g.size(); i++)
                    (i<split?l:r).add(g.get(i));
                sortRanksum(l);
                sortRanksum(r);
                ngroups.add(l);
                ngroups.add(r);
            }
            groups=ngroups;
            if (!good)
                break;
        }
        ArrayList<Integer> ppl=new ArrayList<>();
        for (ArrayList<Integer> g:groups)
            ppl.addAll(g);
        log.append(ppl).append("\n");
        for (int i=0, j=N-1; i<=j;) {
            int t=i/M%2==0?(i % M):(M-1-i%M);
            teams.get(t).add(ppl.get(i));
            if (i<j)
                teams.get(t).add(ppl.get(j));
            i++;
            j--;
        }
        String[] ret = new String[M];
        for (int i=0; i<M; i++) {
            ret[i]=""+teams.get(i).get(0);
            for (int j=1; j<teams.get(i).size(); j++)
                ret[i]+=" "+teams.get(i).get(j);
        }
        //System.out.print(log);
        return ret;
    }
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            int M = Integer.parseInt(br.readLine());
            int N = Integer.parseInt(br.readLine());
            String[] wins = new String[N];
            for (int i = 0; i < N; ++i) {
                wins[i] = br.readLine();
            }
            ContestOrganizer sol = new ContestOrganizer();
            String[] ret = sol.makeTeams(M, wins);
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