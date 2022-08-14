import java.util.*;
import java.io.*;
public class ReversalSort {
    private static final PrintStream P$=System.out;
    private int N, K;
    private long[] C, bcosts;
    private int[] bjumps;
    private long scr(List<int[]> mvs) {
        long out=0;
        for (int[] s:mvs)
            out+=C[s[1]-s[0]+1];
        return out;
    }
    private void pushleft(int[] seq, List<int[]> moves, List<Integer> is, int i) {
        if (is.size()==1) {
            mvleft(seq,moves,is.get(0),i);
            return;
        }
        Collections.sort(is);
        //P$.println("is="+is);
        int S=is.size();
        long[][] dp=new long[S][];
        dp[0]=new long[N-S+1];
        for (int x=i; x+S-1<N; x++)
            dp[0][x]=pushlinecost(x,S,i);
        int[][] choice=new int[S][];
        for (int n=1; n<S; n++) {
            dp[n]=new long[N-(S-n)+1];
            choice[n]=new int[dp[n].length];
            for (int x=is.get(n-1)+1; x+S-n-1<N; x++) {
                int k=S-n;
                long t0=dp[n-1][is.get(n-1)]+pushlinecost(x,k,is.get(n-1)+1),
                        t1=dp[n-1][x-1]+bcosts[(x-1)-is.get(n-1)];
                dp[n][x]=Math.min(t0,t1);
                choice[n][x]=t0<=t1?0:1;
            }
        }
        long cost0=dp[S-1][is.get(S-1)];
        class pii {
            private int a, b;
            private pii(int a, int b) {
                this.a=a; this.b=b;
            }
        }
        List<pii> runs=new ArrayList<>();
        runs.add(new pii(i,0));
        for (int id:is)
            runs.add(new pii(id,1));
        List<int[]> infos=new ArrayList<>();
        long cost1=0;
        for (int rep=0; rep<S; rep++) {
            int bj=-1;
            long bscr=Long.MAX_VALUE;
            for (int j=1; j<runs.size(); j++) {
                long scr=pushlinecost(runs.get(j).a,runs.get(j).b,runs.get(j-1).a+runs.get(j-1).b);
                if (scr<bscr) {
                    bscr=scr;
                    bj=j;
                }
            }
            infos.add(new int[] {runs.get(bj).a,runs.get(bj).b,runs.get(bj-1).a+runs.get(bj-1).b});
            cost1+=bscr;
            runs.get(bj-1).b+=runs.get(bj).b;
            runs.remove(bj);
        }
        if (cost0<=cost1)
            for (int n=S-1, x=is.get(S-1);; n--) {
                if (n==0) {
                    pushlineleft(seq,moves,x,S,i);
                    break;
                }
                //P$.print(choice[n][x]);
                if (choice[n][x]==0) {
                    pushlineleft(seq,moves,x,S-n,is.get(n-1)+1);
                    x=is.get(n-1);
                }
                else {
                    mvright(seq,moves,is.get(n-1),x-1);
                    x--;
                }
            }
        else
            for (int[] info:infos)
                pushlineleft(seq,moves,info[0],info[1],info[2]);
    }
    private long pushlinecost(int a, int k, int c) {
        return Math.min(C[a+k-1-c+1],k*bcosts[a-c]);
    }
    private void pushlineleft(int[] seq, List<int[]> moves, int a, int k, int c) {
        if (a==c) return;
        if (C[a+k-1-c+1]<k*bcosts[a-c])
            domv(seq,moves,c,a+k-1);
        else
            for (int rep=0; rep<k; rep++)
                mvleft(seq,moves,a+rep,c+rep);
    }
    private void group(int[] seq, List<int[]> moves, int G) {
        if (G==K) return;
        for (int lo=0, hi=N-1; lo<hi;) {
            List<int[]> im=new ArrayList<>(), ram=new ArrayList<>();
            List<Integer> mi, ma;
            if (G==1) {
                mi=minis(seq,lo,hi);
                ma=maxis(seq,lo,hi);
            }
            else {
                List<List<Integer>> groups=new ArrayList<>();
                for (int v=0; v<K; v++)
                    groups.add(new ArrayList<>());
                for (int j=lo; j<=hi; j++)
                    groups.get(seq[j]).add(j);
                mi=new ArrayList<>();
                ma=new ArrayList<>();
                for (int cnt=0, v=0; v<K && cnt<G; v++)
                    if (groups.get(v).size()>0) {
                        mi.addAll(groups.get(v));
                        cnt++;
                    }
                for (int cnt=0, v=K-1; v>-1 && cnt<G; v--)
                    if (groups.get(v).size()>0) {
                        ma.addAll(groups.get(v));
                        cnt++;
                    }
            }
            pushleft(null,im,mi,lo);
            List<Integer> rma=new ArrayList<>();
            for (int v:ma) rma.add(N-1-v);
            pushleft(null,ram,rma,N-1-hi);
            if (scr(im)<=scr(ram)) {
                for (int[] m:im)
                    domv(seq,moves,m[0],m[1]);
                lo+=mi.size();
            }
            else {
                for (int[] m:ram)
                    domv(seq,moves,N-1-m[1],N-1-m[0]);
                hi-=ma.size();
            }
        }
    }
    private List<int[]> gineqG(int[] iseq, int G) {
        int[] seq=iseq.clone();
        List<int[]> moves=new ArrayList<>();
        group(seq,moves,G);
        group(seq,moves,1);
        return moves;
    }
    private List<int[]> gin(int[] iseq) {
        int[] seq=iseq.clone();
        List<int[]> moves=new ArrayList<>();
        for (int lo=0, hi=N-1; lo<hi;) {
            int mi=mini(seq,lo,hi), ma=maxi(seq,lo,hi);
            if (bcosts[mi-lo]<=bcosts[hi-ma]) {
                mvleft(seq,moves,mi,lo);
                lo++;
            }
            else {
                mvright(seq,moves,ma,hi);
                hi--;
            }
        }
        return moves;
    }
    private void mvleft(int[] seq, List<int[]> moves, int k, int i) {
        if (k<i) throw new RuntimeException();
        for (int idx=k, j=bjumps[k-i]; idx>i; idx-=j)
            domv(seq,moves,Math.max(i,idx-j),idx);
    }
    private void mvright(int[] seq, List<int[]> moves, int k, int i) {
        if (k>i) throw new RuntimeException();
        for (int idx=k, j=bjumps[i-k]; idx<i; idx+=j)
            domv(seq,moves,idx,Math.min(i,idx+j));
    }
    private static void domv(int[] seq, List<int[]> moves, int l, int r) {
        moves.add(new int[] {l,r});
        if (seq!=null)
            reverse(seq, l, r);
    }
    private static int mini(int[] seq, int i, int j) {
        int min = Integer.MAX_VALUE;
        int k = -1;
        for (int q = i; q <=j; q++)
            if (seq[q] < min) {
                min = seq[q];
                k = q;
            }
        return k;
    }
    private static int maxi(int[] seq, int i, int j) {
        int max = Integer.MIN_VALUE;
        int k = -1;
        for (int q = i; q <=j; q++)
            if (seq[q] > max) {
                max = seq[q];
                k = q;
            }
        return k;
    }
    private static List<Integer> minis(int[] seq, int i, int j) {
        int min = Integer.MAX_VALUE;
        List<Integer> out=new ArrayList<>();
        for (int q = i; q <=j; q++)
            if (seq[q] < min) {
                min = seq[q];
                out.clear();
                out.add(q);
            }
            else if (seq[q]==min)
                out.add(q);
        return out;
    }
    private static List<Integer> maxis(int[] seq, int i, int j) {
        int max = Integer.MIN_VALUE;
        List<Integer> out=new ArrayList<>();
        for (int q = i; q <=j; q++)
            if (seq[q] > max) {
                max = seq[q];
                out.clear();
                out.add(q);
            }
            else if (seq[q]==max)
                out.add(q);
        return out;
    }
    //reverse a sub-sequence between pos1 and pos2, inclusive
    public static void reverse(int[] seq, int pos1, int pos2) {
        for (int i = pos1, k = pos2; i < k; i++, k--) {
            int temp = seq[i];
            seq[i] = seq[k];
            seq[k] = temp;
        }
    }
    public List<int[]> findSolution(int N, int K, double X, int[] seq) {
        long END=System.currentTimeMillis()+9500;
        this.N=N;
        this.K=K;
        C=new long[N+1];
        for (int n=1; n<=N; n++)
            C[n]=(long)Math.pow(n,X);
        bjumps=new int[N];
        bcosts=new long[N];
        for (int dist=0; dist<N; dist++) {
            long bcost=Integer.MAX_VALUE;
            int bjump=-1;
            for (int jump=1; jump<=dist; jump++) {
                long cost=dist/jump*C[jump+1];
                if (dist%jump>0)
                    cost+=C[dist%jump+1];
                if (cost<bcost) {
                    bcost=cost;
                    bjump=jump;
                }
            }
            bjumps[dist]=bjump;
            bcosts[dist]=bcost;
        }
        P$.println("tleft="+(END-System.currentTimeMillis()));
        List<int[]> bmvs=null;
        long bscr=Long.MAX_VALUE;
        int bG=-2;
        {
            List<int[]> mvs=gin(seq);
            long scr=scr(mvs);
            if (scr<bscr) {
                bscr=scr;
                bmvs=mvs;
                bG=0;
            }
        }
        P$.println("bscr="+bscr);
        P$.println("tleft="+(END-System.currentTimeMillis()));
        for (int G=1; G<K && System.currentTimeMillis()<END; G++) {
            List<int[]> mvs=gineqG(seq,G);
            long scr=scr(mvs);
            if (scr<bscr) {
                bscr=scr;
                //if (flip==1) mvs=flipped(mvs);
                bmvs=mvs;
                bG=G;
            }
            //P$.println(G+","+flip+"-->"+ubound);
        }
        P$.println("bG="+bG);
        P$.println("bscr="+bscr);
        P$.println("tleft="+(END-System.currentTimeMillis()));
        List<int[]> pmvs=new ArrayList<>();
        for (int[] pseq=seq.clone(); System.currentTimeMillis()<END;) {
            long pscr=scr(pmvs);
            int[] bnpmv=null;
            List<int[]> bnmvs=null;
            for (int len=2; len<=10 && System.currentTimeMillis()<END; len++)
            for (int i=0; i+len-1<N && System.currentTimeMillis()<END; i++) {
                int j=i+len-1;
                reverse(pseq,i,j);
                List<int[]> nmvs=bG==0?gin(pseq):gineqG(pseq,bG);
                reverse(pseq,i,j);
                long scr=pscr+C[j-i+1]+scr(nmvs);
                if (scr<bscr) {
                    bscr=scr;
                    bnpmv=new int[] {i,j};
                    bnmvs=nmvs;
                }
            }
            if (bnmvs==null) break;
            domv(pseq,pmvs,bnpmv[0],bnpmv[1]);
            bmvs=new ArrayList<>(pmvs);
            bmvs.addAll(bnmvs);
            P$.println("-->"+bscr+" from "+Arrays.toString(bnpmv));
        }
        return bmvs;
    }
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            int N = Integer.parseInt(br.readLine());
            int K = Integer.parseInt(br.readLine());
            double X = Double.parseDouble(br.readLine());
            int[] sequence = new int[N];
            for (int i = 0; i < N; i++) sequence[i] = Integer.parseInt(br.readLine());
            ReversalSort prog = new ReversalSort();
            List<int[]> ret = prog.findSolution(N, K, X, sequence);
            System.out.println(ret.size());
            StringBuilder str=new StringBuilder();
            for (int i = 0; i < ret.size(); i++)
                str.append(ret.get(i)[0]).append(" ").append(ret.get(i)[1]).append("\n");
            System.out.print(str);
        } catch (Exception e) {
        }
    }
}