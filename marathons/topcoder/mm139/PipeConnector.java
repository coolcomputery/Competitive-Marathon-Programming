import java.io.*;
import java.util.*;
public class PipeConnector {
    private static final long HC_TL=9000, TOTAL_TL=9500;
    private static final PrintStream P$=System.err;
    private static final int[] dr={-1,0,0,1}, dc={0,-1,1,0};
    //for d=0...3, (r+dr[d])*N+(c+dc[d]) will be in ascending order
    private static class Path {
        List<Integer> locs;
        int overlaps, idx;
        public Path(List<Integer> ls, int os, int i) {
            locs=ls; overlaps=os; idx=i;
        }
        public Path withIdx(int i) {
            return new Path(locs,0,i);
        }
        public int[] endpts() {
            return new int[] {locs.get(0),locs.get(locs.size()-1)};
        }
        public String toString() {
            return "("+overlaps+") "+locs.toString().replace(", ","-");
        }
    }
    private static int bfscnt=0, pcnt=0;
    private static boolean[] seen;
    private static Path bfspath(int N, int[] layers, boolean[] empty, int s, int e) {
        //find shortest path that does not cross over any other previous paths
        bfscnt++;
        if (s==e) throw new RuntimeException();
        List<Integer> bfs=new ArrayList<>(), par=new ArrayList<>(); bfs.add(s); par.add(-1); seen[s]=true;
        for (int i=0; i<bfs.size(); i++) {
            int v=bfs.get(i);
            if (v==e) {
                for (int u:bfs) seen[u]=false;
                List<Integer> out=new ArrayList<>();
                for (int j=i; j>=0; j=par.get(j)) out.add(bfs.get(j));
                return new Path(out,0,-1);
            }
            for (int d=0; d<4; d++) {
                int nr=v/N+dr[d], nc=v%N+dc[d], nl=nr*N+nc;
                if (0<=nr&&nr<N && 0<=nc&&nc<N && layers[nl]==0 && (empty[nl]||nl==s||nl==e) && !seen[nl]) {
                    bfs.add(nl);
                    par.add(i);
                    seen[nl]=true;
                }
            }
        }
        for (int v:bfs) seen[v]=false;
        return null;
    }
    private static Path path(int N, int[] layers, boolean[] empty, int s, int e) {
        if (s==e) throw new RuntimeException();
        pcnt++;
        int[] cost=new int[N*N], par=new int[N*N];
        Arrays.fill(cost,Integer.MAX_VALUE);
        Arrays.fill(par,-1);
        //WLOG assume P=1
        cost[s]=N*N*layers[s];
        PriorityQueue<Integer> front=new PriorityQueue<>(1, new Comparator<>() {
            public int compare(Integer o1, Integer o2) {
                int d=Integer.compare(cost[o1],cost[o2]);
                return d==0?Integer.compare(o1,o2):d;
            }
        });
        front.add(s);
        while (front.size()>0) {
            int bl=front.poll();
            if (bl==e) break;
            int r=bl/N, c=bl%N;
            for (int d=0; d<4; d++) {
                int nr=r+dr[d], nc=c+dc[d], nl=nr*N+nc;;
                if (0<=nr&&nr<N && 0<=nc&&nc<N && (empty[nl]||nl==s||nl==e)) {
                    int ncost=cost[bl]+1+N*N*layers[nl];
                    if (ncost<cost[nl]) {
                        front.remove(nl);
                        cost[nl]=ncost;
                        par[nl]=bl;
                        front.add(nl);
                    }
                }
            }
        }
        if (cost[e]==Integer.MAX_VALUE) return null;
        List<Integer> out=new ArrayList<>();
        for (int l=e; l>=0; l=par[l]) out.add(l);
        Collections.reverse(out);
        return new Path(out,cost[e]/(N*N),-1);
    }
    private static Path path(int N, int[] layers, boolean[] empty, int[] endpts) {
        return path(N,layers,empty,endpts[0],endpts[1]);
    }
    public static List<List<Integer>> solution(int N, int C, int P, int[][] gridValues, int[][] gridColors) {
        long st=System.currentTimeMillis();
        seen=new boolean[N*N]; //for bfs
        int[] vals=new int[N*N], cols=new int[N*N];
        boolean[] empty=new boolean[N*N];
        for (int i=0; i<N; i++) for (int j=0; j<N; j++) {
            int l=i*N+j;
            vals[l]=gridValues[i][j];
            cols[l]=gridColors[i][j]-1;
            empty[l]=cols[l]<0;
        }
        List<Integer> vertLocs=new ArrayList<>();
        for (int l=0; l<N*N; l++) if (cols[l]>=0) vertLocs.add(l);
        int V=vertLocs.size();
        List<int[]> edges=new ArrayList<>();
        int E; {
            List<int[]> ord=new ArrayList<>();
            int[] blank=new int[N*N];
            for (int a=0; a<V; a++)
                for (int b=0; b<a; b++) {
                    int la=vertLocs.get(a), lb=vertLocs.get(b);
                    if (cols[la]==cols[lb]) {
                        Path p=bfspath(N,blank,empty,la,lb);
                        if (p!=null) {
                            edges.add(new int[] {a,b});
                            ord.add(new int[] {ord.size(),vals[la]*vals[lb]*(N-(p.locs.size()-1))});
                        }
                    }
                }
            E=edges.size();
            P$.println("# verts="+V+" # edges="+E);
            ord.sort(new Comparator<>() {
                public int compare(int[] o1, int[] o2) {
                    int d=-Integer.compare(o1[1],o2[1]);
                    return d==0?Integer.compare(o1[0],o2[0]):d;
                }
            });
            List<int[]> tmp=new ArrayList<>();
            for (int i=0; i<edges.size(); i++) tmp.add(edges.get(ord.get(i)[0]));
            edges.clear();
            edges.addAll(tmp);
        }
        List<List<Integer>> eisAtVert=new ArrayList<>();
        for (int v=0; v<V; v++) eisAtVert.add(new ArrayList<>());
        for (int ei=0; ei<E; ei++) {
            int[] e=edges.get(ei);
            eisAtVert.get(e[0]).add(ei);
            eisAtVert.get(e[1]).add(ei);
        }
        class Sol {
            Path[] paths=new Path[E];
            int scr=0;
            int[] layers=new int[N*N];
            List<Set<Integer>> eisAtLoc;
            Sol() {
                eisAtLoc=new ArrayList<>();
                for (int l=0; l<N*N; l++) eisAtLoc.add(new HashSet<>());
            }
            public boolean pairOpen(int ei) {
                int[] e=edges.get(ei);
                int la=vertLocs.get(e[0]), lb=vertLocs.get(e[1]);
                return layers[la]==0&&layers[lb]==0;
            }
            public void addPath(Path p) {
                int ei=p.idx;
                int[] e=edges.get(ei);
                int la=vertLocs.get(e[0]), lb=vertLocs.get(e[1]);
                if (layers[la]==0&&layers[lb]==0) {
                    scr+=vals[la]*vals[lb];
                    for (int l:p.locs) {
                        scr-=P*layers[l];
                        layers[l]++;
                        eisAtLoc.get(l).add(ei);
                    }
                    paths[ei]=p.withIdx(ei);
                }
                else throw new RuntimeException();
            }
            public Path addGreedyPath(int ei) {
                int[] e=edges.get(ei);
                int la=vertLocs.get(e[0]), lb=vertLocs.get(e[1]);
                if (layers[la]==0&&layers[lb]==0) {
                    Path p=path(N,layers,empty,la,lb);
                    int ds=vals[la]*vals[lb]-P*p.overlaps;
                    if (ds>0) {
                        scr+=ds;
                        for (int l:p.locs) {
                            layers[l]++;
                            eisAtLoc.get(l).add(ei);
                        }
                        paths[ei]=p.withIdx(ei);
                        return paths[ei];
                    }
                    return p;
                }
                else throw new RuntimeException();
            }
            public Path remPath(int ei) {
                int[] e=edges.get(ei);
                int la=vertLocs.get(e[0]), lb=vertLocs.get(e[1]);
                Path op=paths[ei];
                if (op!=null) {
                    scr-=vals[la]*vals[lb];
                    for (int l:op.locs) {
                        layers[l]--;
                        eisAtLoc.get(l).remove(ei);
                        scr+=P*layers[l];
                    }
                    paths[ei]=null;
                }
                return op;
            }
            public Sol(int[] eis, boolean replace) {
                this();
                for (int ei:eis) if (pairOpen(ei)) {
                    Path p=addGreedyPath(ei);
                    if (replace&&p.idx<0) {
                        int oscr=scr;
                        Set<Integer> badIdxs=new TreeSet<>();
                        for (int l:p.locs) badIdxs.addAll(eisAtLoc.get(l));
                        List<Path> ops=new ArrayList<>();
                        for (int e:badIdxs) ops.add(remPath(e));
                        addGreedyPath(ei);
                        for (int e:badIdxs) addGreedyPath(e);
                        if (scr<oscr) {
                            remPath(ei);
                            for (int e:badIdxs) remPath(e);
                            for (Path op:ops) addPath(op);
                        }
                    }
                }
            }
        }
        P$.println("small hill climbing");
        SplittableRandom rnd=new SplittableRandom(1);
        Sol hc; {
            int[] eis=new int[E]; for (int ei=0; ei<E; ei++) eis[ei]=ei;
            Sol hc_replacing=new Sol(eis,true);
            P$.println("greedy+replacing strategy: scr="+hc_replacing.scr);
            hc=new Sol(eis,false);
            long reps=0, accn=0;
            P$.println("t="+(System.currentTimeMillis()-st)+" reps="+reps+" accn="+accn+" scr="+hc.scr);
            for (; System.currentTimeMillis()-st<1000; reps++) {
                int[] neis=Arrays.copyOf(eis,E);
                for (int r=0; r<1+V/10; r++) {
                    int i=rnd.nextInt(E), j=rnd.nextInt(E-1);
                    if (j>=i) j++;
                    int t=neis[i]; neis[i]=neis[j]; neis[j]=t;
                }
                Sol nhc=new Sol(neis,false);
                if (nhc.scr>=hc.scr) {
                    hc=nhc;
                    eis=neis;
                    accn++;
                }
            }
            P$.println("t="+(System.currentTimeMillis()-st)+" reps="+reps+" accn="+accn+" scr="+hc.scr);
            if (hc_replacing.scr>hc.scr) {
                hc=hc_replacing;
                P$.println("use greedy+replacing strategy");
            }
        }
        class Help {
            private double avg=Math.sqrt(N)-1;
            public int[] geoRndIntv(int N) {
                int len=1;
                while (len<N&&rnd.nextDouble()>1.0/avg) len++;
                int i=rnd.nextInt(N-len+1);
                return new int[] {i,i+len-1};
            }
        } Help $=new Help();
        P$.println("main hill climbing");
        long[] reps=new long[2], accn=new long[2];
        for (long tot_reps=0, mark=0;; tot_reps++) {
            long tm=System.currentTimeMillis()-st;
            if (tm>=mark||tm>=HC_TL) {
                P$.printf("t=%d reps=%d %s accn=%s scr=%d%n",tm,tot_reps,
                        Arrays.toString(reps).replace(" ",""),Arrays.toString(accn).replace(" ",""),hc.scr);
                while (tm>=mark) mark+=1000;
                if (tm>=HC_TL) break;
            }
            int oscr=hc.scr;
            List<Path> oldPaths=new ArrayList<>();
            int[] rrange=$.geoRndIntv(N), crange=$.geoRndIntv(N);
            Set<Integer> oeis=new HashSet<>();
            for (int r=rrange[0]; r<=rrange[1]; r++) for (int c=crange[0]; c<=crange[1]; c++)
                oeis.addAll(hc.eisAtLoc.get(r*N+c));
            for (int ei:oeis) oldPaths.add(hc.remPath(ei));
            List<Integer> neis;
            int type=rnd.nextDouble()<0.5?1:0;
            if (type==0) {
                neis=new ArrayList<>(oeis);
                for (int i=neis.size()-1; i>0; i--) {
                    int j=rnd.nextInt(i+1);
                    int t=neis.get(i); neis.set(i,neis.get(j)); neis.set(j,t);
                }
            }
            else {
                List<Integer> sverts=new ArrayList<>();
                for (int ei:oeis) {
                    sverts.add(edges.get(ei)[0]); sverts.add(edges.get(ei)[1]);
                }
                int uscr, t=0;
                do {
                    for (int i=sverts.size()-1; i>0; i--) {
                        int j=rnd.nextInt(i+1);
                        int tmp=sverts.get(i); sverts.set(i,sverts.get(j)); sverts.set(j,tmp);
                    }
                    boolean[] taken=new boolean[V]; for (int v=0; v<V; v++) taken[v]=hc.layers[vertLocs.get(v)]>0;
                    neis=new ArrayList<>();
                    uscr=hc.scr;
                    for (int v:sverts) if (!taken[v]) {
                        List<Integer> veis=new ArrayList<>();
                        for (int ei:eisAtVert.get(v)) {
                            int[] e=edges.get(ei);
                            int n=e[0]==v?e[1]:e[0];
                            if (!taken[n]) veis.add(ei);
                        }
                        if (veis.size()>0) {
                            int ne=veis.get(rnd.nextInt(veis.size()));
                            neis.add(ne);
                            int a=edges.get(ne)[0], b=edges.get(ne)[1];
                            taken[a]=true; taken[b]=true;
                            uscr+=vals[vertLocs.get(a)]*vals[vertLocs.get(b)];
                        }
                    }
                    t++;
                } while (uscr<hc.scr&&t<100);
                if (uscr<hc.scr) neis.clear();
            }
            for (int ei:neis) hc.addGreedyPath(ei);
            if (hc.scr>=oscr) accn[type]++;
            else {
                for (int ei:neis) hc.remPath(ei);
                for (Path op:oldPaths) hc.addPath(op);
                if (hc.scr!=oscr) throw new RuntimeException();
            }
            reps[type]++;
        }


        List<Path> sol=new ArrayList<>(); for (Path p:hc.paths) if (p!=null) sol.add(p);
        final int[] scr = {hc.scr};
        int[] layers=new int[N*N]; for (Path p:sol) for (int l:p.locs) layers[l]++;
        class Adder {
            public void add() {
                P$.println("adding any possible new pipes");
                //add any pipes that were missed during hill climbing
                List<Set<Integer>> pathIdxss=new ArrayList<>();
                for (int l=0; l<N*N; l++) pathIdxss.add(new HashSet<>());
                for (int pi=0; pi<sol.size(); pi++) for (int l:sol.get(pi).locs) pathIdxss.get(l).add(pi);
                for (int[] e:edges) {
                    if (System.currentTimeMillis()-st>=TOTAL_TL) break;
                    int la=vertLocs.get(e[0]), lb=vertLocs.get(e[1]);
                    if (layers[la]==0&&layers[lb]==0) {
                        Path p=path(N,layers,empty,la,lb);
                        int ds=vals[la]*vals[lb]-P*p.overlaps;
                        List<Integer> resetIdxs=new ArrayList<>();
                        List<Path> resetPaths=new ArrayList<>();
                        if (p.overlaps>0) {
                            Set<Integer> badIdxs=new HashSet<>();
                            for (int l:p.locs) badIdxs.addAll(pathIdxss.get(l));
                            resetIdxs=new ArrayList<>(badIdxs);
                            int[] nlayers=Arrays.copyOf(layers,N*N);
                            ds=0;
                            for (int pi:resetIdxs) {
                                int[] endpts=sol.get(pi).endpts();
                                ds-=vals[endpts[0]]*vals[endpts[1]];
                                for (int l:sol.get(pi).locs) {
                                    nlayers[l]--;
                                    ds+=P*nlayers[l];
                                }
                            }
                            p=path(N,nlayers,empty,la,lb);
                            ds+=vals[la]*vals[lb]-P*p.overlaps;
                            for (int l:p.locs) nlayers[l]++;
                            for (int pi:resetIdxs) {
                                int[] endpts=sol.get(pi).endpts();
                                Path np=path(N,nlayers,empty,endpts);
                                resetPaths.add(np);
                                ds+=vals[endpts[0]]*vals[endpts[1]]-P*np.overlaps;
                                for (int l:np.locs) nlayers[l]++;
                            }
                        }
                        if (ds>0) {
                            scr[0] +=ds;
                            P$.println("scr="+ scr[0] +" "+p);
                            for (int l:p.locs) {
                                layers[l]++;
                                pathIdxss.get(l).add(sol.size());
                            }
                            sol.add(p);
                            for (int ri=0; ri<resetIdxs.size(); ri++) {
                                int pi=resetIdxs.get(ri);
                                Path op=sol.get(pi);
                                for (int l:op.locs) {
                                    layers[l]--;
                                    pathIdxss.get(l).remove(pi);
                                }
                                Path np=resetPaths.get(ri);
                                sol.set(pi,np);
                                for (int l:np.locs) {
                                    layers[l]++;
                                    pathIdxss.get(l).add(pi);
                                }
                                P$.println("    "+op+" --> "+np);
                            }
                        }
                    }
                }
            }
        }
        new Adder().add();

        //2-opt
        P$.println("2-opt");
        int reps_2opt=0;
        List<int[]> pipairs=new ArrayList<>();
        for (int pia=0; pia<sol.size(); pia++) for (int pib=0; pib<pia; pib++)
            if (cols[sol.get(pia).endpts()[0]]==cols[sol.get(pib).endpts()[0]])
                pipairs.add(new int[] {pia,pib});
        int pipairs_i=0;
        for (; System.currentTimeMillis()-st<TOTAL_TL && pipairs_i<pipairs.size(); reps_2opt++) {
            int[] pipair=pipairs.get(pipairs_i);
            int a=sol.get(pipair[0]).endpts()[0], b=sol.get(pipair[0]).endpts()[1],
                    c=sol.get(pipair[1]).endpts()[0], d=sol.get(pipair[1]).endpts()[1];
            Path p0=sol.get(pipair[0]), p1=sol.get(pipair[1]);
            int bscr=vals[a]*vals[b]+vals[c]*vals[d];
            for (int l:p0.locs) {
                layers[l]--;
                bscr-=P*layers[l];
            }
            for (int l:p1.locs) {
                layers[l]--;
                bscr-=P*layers[l];
            }
            int bscr0=bscr;
            List<Path> bpaths=new ArrayList<>(Arrays.asList(p0,p1));
            for (int[][] ping:new int[][][] {{{a,c},{b,d}},{{a,d},{b,c}}}) {
                int nscr=vals[ping[0][0]]*vals[ping[0][1]]+vals[ping[1][0]]*vals[ping[1][1]];
                if (nscr>bscr) {
                    int[] nlayers=Arrays.copyOf(layers,N*N);
                    Path np0=path(N,nlayers,empty,ping[0]);
                    if (np0!=null) {
                        nscr-=P*np0.overlaps;
                        for (int l:np0.locs) nlayers[l]++;
                        Path np1=path(N,nlayers,empty,ping[1]);
                        if (np1!=null) {
                            nscr-=P*np1.overlaps;
                            for (int l:np1.locs) nlayers[l]++;
                            List<Path> paths=new ArrayList<>(Arrays.asList(np0,np1));
                            if (nscr>bscr) {
                                bscr=nscr;
                                bpaths=paths;
                            }
                        }
                    }
                }
            }
            sol.set(pipair[0],bpaths.get(0));
            sol.set(pipair[1],bpaths.get(1));
            for (int l:bpaths.get(0).locs) layers[l]++;
            for (int l:bpaths.get(1).locs) layers[l]++;
            if (bscr>bscr0) {
                scr[0] +=bscr-bscr0;
                P$.printf("scr=%d %s,%s%n    -->%s,%s%n", scr[0],p0,p1,bpaths.get(0),bpaths.get(1));
                pipairs_i=0;
                pipairs=new ArrayList<>();
                for (int pia=0; pia<sol.size(); pia++) for (int pib=0; pib<pia; pib++)
                    if (cols[sol.get(pia).endpts()[0]]==cols[sol.get(pib).endpts()[0]])
                        pipairs.add(new int[] {pia,pib});
            }
            else pipairs_i++;
        }
        P$.println("# 2-opt reps="+reps_2opt);

        new Adder().add();

        P$.println("# calls to bfspath(),path()="+bfscnt+","+pcnt);
        List<List<Integer>> ret=new ArrayList<>();
        for (Path p:sol) ret.add(p.locs);
        return ret;
    }
    public static void main(String[] args) throws Exception {
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        int N=Integer.parseInt(in.readLine());
        int C=Integer.parseInt(in.readLine());
        int P=Integer.parseInt(in.readLine());
        int[][] gridValues=new int[N][N];
        int[][] gridColours=new int[N][N];
        for (int r=0; r<N; r++)
            for (int c=0; c<N; c++) {
                String[] temp=in.readLine().split(" ");
                gridValues[r][c]=Integer.parseInt(temp[0]);
                gridColours[r][c]=Integer.parseInt(temp[1]);
            }
        List<List<Integer>> paths=solution(N,C,P,gridValues,gridColours);
        System.out.println(paths.size());
        for (List<Integer> p:paths) {
            System.out.println(p.size());
            for (int l:p) System.out.println((l/N)+" "+(l%N));
        }
        System.out.flush();
    }
}