import java.io.*;
import java.util.*;
public class A {
    public static void main(String[] args) throws IOException {
        long st=System.currentTimeMillis();
        String f$="a1";
        System.out.println("f$="+f$);
        BufferedReader in=new BufferedReader(new FileReader(f$+".in"));
        E=0;
        while (true) {
            String s=in.readLine();
            if (s==null) break;
            E++;
        }
        System.out.println("E="+E);
        in=new BufferedReader(new FileReader(f$+".in"));
        edges=new int[E][];
        for (int e=0; e<E; e++) {
            StringTokenizer tok=new StringTokenizer(in.readLine());
            edges[e]=new int[] {Integer.parseInt(tok.nextToken()),
                    Integer.parseInt(tok.nextToken())};
        }
        System.out.println("read edges: time="+(System.currentTimeMillis()-st));
        V=0;
        for (int[] e:edges)
            V=Math.max(V,Math.max(e[0],e[1]));
        V++;
        deg=new int[V];
        for (int[] e:edges) {
            deg[e[0]]++;
            deg[e[1]]++;
        }
        adjss=new int[V][];
        for (int i=0; i<V; i++)
            adjss[i]=new int[deg[i]];
        {
            int[] idxs=new int[V];
            for (int e=0; e<E; e++) {
                int a=edges[e][0], b=edges[e][1];
                adjss[a][idxs[a]++]=b;
                adjss[b][idxs[b]++]=a;
            }
        }
        System.out.println("V="+V);
        System.out.println("build graph: time="+(System.currentTimeMillis()-st));
        group=new int[V];
        for (int i=0; i<V; i++)
            group[i]=i;
        {
            in=new BufferedReader(new FileReader(f$+".out"));
            for (int gi=0; true; gi++) {
                String s=in.readLine();
                if (s==null) break;
                StringTokenizer tok=new StringTokenizer(s);
                while (tok.hasMoreTokens())
                    group[Integer.parseInt(tok.nextToken())]=gi;
            }
        }
        System.out.println("initscr="+scr(info(group)));
        /*agglom(100_000,5_000);
        greedy(100_000,5_000);*/
        //SA
        SplittableRandom rnd=new SplittableRandom(1);
        double[] info=info(group);
        double scr=scr(info);
        long REPS=0, ACC0=0, ACC1=0;
        String form="%12d%12d%12d%12.8f%n";
        long TIME=System.currentTimeMillis(), TL=60_000, END=TIME+TL,
                TLOG=2_000,LOG=TIME+TLOG;
        System.out.println("SA: TL="+TL+", TLOG="+TLOG);
        System.out.printf(form,REPS,ACC0,ACC1,scr);
        boolean[] taken=new boolean[V];
        while (true) {
            TIME=System.currentTimeMillis();
            if (TIME>END) break;
            int v$=rnd.nextInt(V);
            double temp=0.0000001*(END-TIME)/TL;
            int ogi=group[v$];
            List<Integer> vs=new ArrayList<>(), depths=new ArrayList<>();
            vs.add(v$);
            taken[v$]=true;
            depths.add(0);
            int d=rnd.nextInt(3);//rnd.nextInt(rnd.nextInt(4);
            for (int i=0; i<vs.size() && depths.get(i)<d; i++) {
                int vi=vs.get(i);
                for (int vj:adjss[vi])
                    if (!taken[vj] && group[vj]==ogi) {
                        vs.add(vj);
                        taken[vj]=true;
                        depths.add(depths.get(i)+1);
                    }
            }
            for (int vi:vs)
                taken[vi]=false;
            int gi;
            {

                int vi=vs.get(rnd.nextInt(vs.size()));
                gi=group[adjss[vi][rnd.nextInt(deg[vi])]];
                if (gi==ogi) {
                    gi=group[rnd.nextInt(V-1)];
                    if (gi==ogi) {
                        gi=rnd.nextInt(V-1);
                        if (gi>=ogi) gi++;
                    }
                }
            }
            int ein_ogi=ein[ogi], ein_gi=ein[gi];
            int sdeg_ogi=sdeg[ogi], sdeg_gi=sdeg[gi];
            int sz_ogi=sz[ogi], sz_gi=sz[gi];
            for (int vi:vs) {
                if (group[vi]!=ogi) throw new RuntimeException();
                for (int vj:adjss[vi]) {
                    if (group[vj]==ogi)
                        ein_ogi--;
                    else if (group[vj]==gi)
                        ein_gi++;
                }
                group[vi]=gi;
                sdeg_ogi-=deg[vi]; sdeg_gi+=deg[vi];
                sz_ogi--; sz_gi++;
            }
            double[] ninfo=info.clone();
            ninfo[0]+=mod_scr(ein_ogi,sdeg_ogi)+mod_scr(ein_gi,sdeg_gi);
            ninfo[0]-=mod_scr(ein[ogi],sdeg[ogi])+mod_scr(ein[gi],sdeg[gi]);
            ninfo[1]+=dens(ein_ogi,sz_ogi)+dens(ein_gi,sz_gi);
            ninfo[1]-=dens(ein[ogi],sz[ogi])+dens(ein[gi],sz[gi]);
            if (sz_ogi==0)
                ninfo[2]--;
            if (sz[gi]==0)
                ninfo[2]++;
            double nscr=scr(ninfo);
            if (nscr>=scr
                    || rnd.nextDouble()<Math.exp((nscr-scr)/temp)
                    ) {
                if (nscr>=scr) ACC0++; else ACC1++;
                info=ninfo;
                scr=nscr;
                ein[ogi]=ein_ogi;
                ein[gi]=ein_gi;
                sdeg[ogi]=sdeg_ogi;
                sdeg[gi]=sdeg_gi;
                sz[ogi]=sz_ogi;
                sz[gi]=sz_gi;
            }
            else {
                for (int vi:vs)
                    group[vi]=ogi;
            }
            REPS++;
            if (TIME>LOG) {
                LOG+=TLOG;
                System.out.printf(form.substring(0,form.length()-2)+"%20.16f%n",REPS,ACC0,ACC1,scr,temp);
            }
        }
        System.out.printf(form,REPS,ACC0,ACC1,scr);
        System.out.println("info="+Arrays.toString(info));
        double[] rinfo=info(group);
        System.out.println("rinfo="+Arrays.toString(rinfo));
        System.out.println("rscr="+scr(rinfo));
        //\SA
        for (int $=0; $<300; $++) {
            boolean b=false;
            if (agglom(100_000,10_000)) b=true;
            if (greedy(100_000,10_000)) b=true;
            if (!b) break;
        }
        List<List<Integer>> groups=new ArrayList<>();
        for (int i=0; i<V; i++)
            groups.add(new ArrayList<>());
        for (int i=0; i<V; i++)
            groups.get(group[i]).add(i);
        StringBuilder s=new StringBuilder();
        for (List<Integer> g:groups)
            if (g.size()>0) {
                for (int i=0; i<g.size(); i++)
                    s.append(i>0?" ":"").append(g.get(i));
                s.append("\n");
            }
        PrintWriter out=new PrintWriter(new FileWriter(f$+".out"));
        out.print(s);
        out.close();
        System.out.println("time="+(System.currentTimeMillis()-st));
    }
    private static int V, E;
    private static int[][] edges, adjss;
    private static int[] deg;
    private static boolean agglom(long TL, long TLOG) {
        long TIME=System.currentTimeMillis(), END=TIME+TL, LOG=TIME+TLOG;
        double[] info=info(group);
        long REPS=0, ACCN=0;
        System.out.println("agglom:TL="+TL+",TLOG="+TLOG);
        String form="%12d%12d%12.8f%n";
        System.out.printf(form,REPS,ACCN,scr(info));
        List<List<Integer>> gverts=new ArrayList<>();
        for (int i=0; i<V; i++)
            gverts.add(new ArrayList<>());
        for (int i=0; i<V; i++)
            gverts.get(group[i]).add(i);
        boolean ret=false;
        while (true) {
            TIME=System.currentTimeMillis();
            if (TIME>END) break;
            boolean found=false;
            for (int $g0=0; $g0<V && !found; $g0++)
                if (sz[$g0]>0)
                    for (int $g1=0; $g1<$g0 && !found; $g1++)
                        if (sz[$g1]>0) { //TRY TO COMBINE TWO ENTIRE GROUPS TOGETHER
                            int g0=$g0, g1=$g1;
                            if (sz[g0]>sz[g1]) {
                                int t=g0;
                                g0=g1;
                                g1=t;
                            }
                            int ein0=ein[g0], sdeg0=sdeg[g0], sz0=sz[g0];
                            int ein1=ein[g1], sdeg1=sdeg[g1], sz1=sz[g1];
                            for (int vi:gverts.get(g0)) {
                                for (int vj:adjss[vi]) {
                                    if (group[vj]==g0)
                                        ein[g0]--;
                                    else if (group[vj]==g1)
                                        ein[g1]++;
                                }
                                sdeg[g0]-=deg[vi]; sdeg[g1]+=deg[vi];
                                sz[g0]--; sz[g1]++;
                                group[vi]=g1;
                            }
                            double[] ninfo=info.clone();
                            ninfo[0]+=mod_scr(ein[g0],sdeg[g0])+mod_scr(ein[g1],sdeg[g1]);
                            ninfo[0]-=mod_scr(ein0,sdeg0)+mod_scr(ein1,sdeg1);
                            ninfo[1]+=dens(ein[g0],sz[g0])+dens(ein[g1],sz[g1]);
                            ninfo[1]-=dens(ein0,sz0)+dens(ein1,sz1);
                            ninfo[2]--;
                            double nscr=scr(ninfo);
                            if (nscr>=scr(info)) {
                                info=ninfo;
                                gverts.get(g1).addAll(gverts.get(g0));
                                gverts.get(g0).clear();
                                found=true;
                                ACCN++;
                                ret=true;
                            }
                            else {
                                ein[g0]=ein0; sdeg[g0]=sdeg0; sz[g0]=sz0;
                                ein[g1]=ein1; sdeg[g1]=sdeg1; sz[g1]=sz1;
                                for (int vi:gverts.get(g0))
                                    group[vi]=g0;
                            }
                            REPS++;
                            if (TIME>LOG) {
                                LOG+=TLOG;
                                System.out.printf(form,REPS,ACCN,scr(info));
                            }
                        }
            if (!found) break;
        }
        System.out.printf(form,REPS,ACCN,scr(info));
        System.out.println("info="+Arrays.toString(info));
        int[][] tmp={ein.clone(),sdeg.clone(),sz.clone()};
        double[] rinfo=info(group);
        System.out.println("rinfo="+Arrays.toString(rinfo));
        System.out.println("rscr="+scr(rinfo));
        String err="";
        if (!eq(tmp[0],ein)) err+="ein;";
        if (!eq(tmp[1],sdeg)) err+="sdeg;";
        if (!eq(tmp[2],sz)) err+="sz;";
        if (err.length()>0)
            throw new RuntimeException(err);
        return ret;
    }
    private static boolean greedy(long TL, long TLOG) {
        long TIME=System.currentTimeMillis(), END=TIME+TL, LOG=TIME+TLOG;
        double[] info=info(group);
        long REPS=0, ACCN=0;
        System.out.println("greedy:TL="+TL+",TLOG="+TLOG);
        String form="%12d%12d%12.8f%n";
        System.out.printf(form,REPS,ACCN,scr(info));
        boolean ret=false;
        for (int vi=0, mode=0, fail_streak=0; true; vi=(vi+1)%V) {
            TIME=System.currentTimeMillis();
            if (TIME>END) break;
            int ogi=group[vi];
            Set<Integer> cols=new HashSet<>();
            if (mode==0)
                for (int vj:adjss[vi]) {
                    if (group[vj]!=ogi)
                        cols.add(group[vj]);
                }
            else
                for (int i=0; i<V; i++)
                    if (i!=ogi)
                        cols.add(i);
            if (cols.size()>0) {
                double[] binfo=info.clone();
                int bgi=-1;
                boolean strict=false;
                for (int gi:cols) {
                    int ein_ogi=ein[ogi], ein_gi=ein[gi];
                    int sdeg_ogi=sdeg[ogi], sdeg_gi=sdeg[gi];
                    int sz_ogi=sz[ogi], sz_gi=sz[gi];
                    for (int vj:adjss[vi]) {
                        if (group[vj]==ogi)
                            ein[ogi]--;
                        else if (group[vj]==gi)
                            ein[gi]++;
                    }
                    sdeg[ogi]-=deg[vi]; sdeg[gi]+=deg[vi];
                    sz[ogi]--; sz[gi]++;
                    double[] ninfo=info.clone();
                    ninfo[0]+=mod_scr(ein[ogi],sdeg[ogi])+mod_scr(ein[gi],sdeg[gi]);
                    ninfo[0]-=mod_scr(ein_ogi,sdeg_ogi)+mod_scr(ein_gi,sdeg_gi);
                    ninfo[1]+=dens(ein[ogi],sz[ogi])+dens(ein[gi],sz[gi]);
                    ninfo[1]-=dens(ein_ogi,sz_ogi)+dens(ein_gi,sz_gi);
                    if (sz[ogi]==0)
                        ninfo[2]--;
                    if (sz[gi]==1)
                        ninfo[2]++;
                    double nscr=scr(ninfo);
                    if (nscr>=scr(binfo)) {
                        if (nscr>scr(binfo)) strict=true;
                        binfo=ninfo;
                        bgi=gi;
                    }
                    ein[ogi]=ein_ogi;
                    ein[gi]=ein_gi;
                    sdeg[ogi]=sdeg_ogi;
                    sdeg[gi]=sdeg_gi;
                    sz[ogi]=sz_ogi;
                    sz[gi]=sz_gi;
                }
                if (bgi!=-1) {
                    int gi=bgi;
                    for (int vj:adjss[vi]) {
                        if (group[vj]==ogi)
                            ein[ogi]--;
                        else if (group[vj]==gi)
                            ein[gi]++;
                    }
                    sdeg[ogi]-=deg[vi]; sdeg[gi]+=deg[vi];
                    sz[ogi]--; sz[gi]++;
                    group[vi]=bgi;
                    info=binfo;
                    ACCN++;
                    if (strict) ret=true;
                    fail_streak=0;
                }
                else
                    fail_streak++;
                REPS++;
            }
            if (TIME>LOG) {
                LOG+=TLOG;
                System.out.printf(form,REPS,ACCN,scr(info));
            }
            if (fail_streak==V) {
                mode++;
                fail_streak=0;
                if (mode==2)
                    break;
            }
        }
        System.out.printf(form,REPS,ACCN,scr(info));
        System.out.println("info="+Arrays.toString(info));
        double[] rinfo=info(group);
        System.out.println("rinfo="+Arrays.toString(rinfo));
        System.out.println("rscr="+scr(rinfo));
        return ret;
    }
    private static boolean eq(int[] a, int[] b) {
        if (a.length!=b.length) return false;
        for (int i=0; i<a.length; i++)
            if (a[i]!=b[i]) return false;
        return true;
    }
    private static double sq(double n) {
        return n*n;
    }
    private static double mod_scr(int ein, int sdeg) {
        return ein/(double)E-sq(sdeg/(2.0*E));
    }
    private static double dens(int ein, int sz) {
        return (sz==0?0:sz==1?1:(ein/(0.5*sz*(sz-1))));
    }
    private static double scr(double[] info) {
        return info[0]+0.5*(info[1]/info[2]-1.0/(V/info[2]));
    }
    private static double[] info(int[] ein, int[] sdeg, int[] sz) {
        double out=0;
        for (int c=0; c<V; c++)
            if (sz[c]>0)
                out+=mod_scr(ein[c],sdeg[c]);
        double reg=0;
        int n=0;
        for (int c=0; c<V; c++)
            if (sz[c]>0)
                n++;
        for (int c=0; c<V; c++)
            if (sz[c]>0)
                reg+=dens(ein[c],sz[c]);
        return new double[] {out,reg,n};
    }
    private static int[] ein, sdeg, sz;
    private static int[] group;
    private static double[] info(int[] group) {
        //ea. vert. in group id in range [0,V)
        ein=new int[V];
        sdeg=new int[V];
        sz=new int[V];
        for (int i=0; i<V; i++) {
            sdeg[group[i]]+=deg[i];
            sz[group[i]]++;
        }
        for (int[] e:edges)
            if (group[e[0]]==group[e[1]])
                ein[group[e[0]]]++;
        return info(ein,sdeg,sz);
    }
}