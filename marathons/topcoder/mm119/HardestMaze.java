import java.util.*;
import java.io.*;
public class HardestMaze {
    private static final PrintStream P$=System.out;
    private static final int INF=1000000;
    private static final int[] dr = {0,1,0,-1};     //L, U, R, D
    private static final int[] dc = {-1,0,1,0};
    private int N, R, T;
    private long END;
    private int[][] starts;
    private int[][][] targets;
    private static boolean[][] smat(boolean[][] a, int x0, int x1, int y0, int y1) {
        boolean[][] b=new boolean[x1-x0+1][y1-y0+1];
        for (int i=x0; i<=x1; i++)
            for (int j=y0; j<=y1; j++)
                b[i-x0][j-y0]=a[i][j];
        return b;
    }
    private static void paste(boolean[][] a, boolean[][] b, int x0, int y0) {
        for (int i=0; i<b.length; i++)
            for (int j=0; j<b[0].length; j++)
                a[i+x0][j+y0]=b[i][j];
    }
    private static boolean[][] transs(int bits, boolean[][] a) {
        int M=a.length, N=a[0].length;
        boolean[][] b=(bits&1)==0?new boolean[M][N]:new boolean[N][M];
        for (int i=0; i<M; i++)
            for (int j=0; j<N; j++) {
                int i1=i, j1=j;
                int mM=M, mN=N;
                if ((bits&1)!=0) {
                    int t=i1; i1=j1; j1=t;
                    t=mM; mM=mN; mN=t;
                }
                if ((bits&2)!=0)
                    i1=mM-1-i1;
                if ((bits&4)!=0)
                    j1=mN-1-j1;
                b[i1][j1]=a[i][j];
            }
        return b;
    }
    private static boolean[][] itranss(int bits, boolean[][] a) {
        int M=a.length, N=a[0].length;
        boolean[][] b=(bits&1)==0?new boolean[M][N]:new boolean[N][M];
        for (int i=0; i<M; i++)
            for (int j=0; j<N; j++) {
                int i1=i, j1=j;
                if ((bits&4)!=0)
                    j1=N-1-j1;
                if ((bits&2)!=0)
                    i1=M-1-i1;
                if ((bits&1)!=0) {
                    int t=i1; i1=j1; j1=t;
                }
                b[i1][j1]=a[i][j];
            }
        return b;
    }
    //taken from Topcoder tester
    private int scr(boolean[][] maze, int[][] starts, int[][][] targets) {
        if (maze==null) return Integer.MIN_VALUE;
        boolean[] needed=new boolean[N*N];
        for (int[] p:starts)
            needed[p[0]*N+p[1]]=true;
        for (int[][] r:targets)
            for (int[] p:r)
                needed[p[0]*N+p[1]]=true;
        int[][] dist=new int[N*N][];
        for (int i=0; i<dist.length; i++)
            if (needed[i]) {
                if (maze[i/N][i%N]) return -1;
                dist[i]=new int[N*N];
                Arrays.fill(dist[i],INF);
                dist[i][i]=0;
                boolean[] seen=new boolean[N*N];
                int[] verts=new int[N*N];
                int vsz=0;
                verts[vsz++]=i;
                seen[i]=true;
                for (int oi=0; oi<vsz; oi++) {
                    int v=verts[oi], r=v/N, c=v%N;
                    for (int d=0; d<4; d++) {
                        int r1=r+dr[d], c1=c+dc[d], v1=r1*N+c1;
                        if (inGrid(r1,c1) && !maze[r1][c1] && !seen[v1]) {
                            seen[v1]=true;
                            dist[i][v1]=dist[i][v]+1;
                            verts[vsz++]=v1;
                        }
                    }
                }
            }
        int out=0;
        for (int q=0; q<R; q++) {
            int startId=starts[q][0]*N+starts[q][1];
            //check that all targets are reachable
            for (int i=0; i<T; i++) {
                int targetId=targets[q][i][0]*N+targets[q][i][1];
                if (dist[startId][targetId]>=INF)
                    return -1;
            }
            int best=Integer.MAX_VALUE;
            int[] ind=new int[T];
            for (int i=0; i<T; i++) ind[i]=i;
            do {
                int total=0;
                int prevId=startId;
                for (int i=0; i<T; i++) {
                    int nextId=targets[q][ind[i]][0]*N+targets[q][ind[i]][1];
                    total+=dist[prevId][nextId];
                    prevId=nextId;
                }
                best=Math.min(best,total);
            }
            while(nextPermutation(ind));
            out+=best;
        }
        return out;
    }
    private boolean inGrid(int x, int y) {
        return x >= 0 && x < N && y >= 0 && y < N;
    }
    private static boolean nextPermutation(int[] a) {
        int n=a.length;
        int i=n-2;
        for (; i>=0; i--)
            if (a[i]<a[i+1])
                break;
        if (i<0) return false;
        for (int j=n-1; j>=i; j--) {
            if (a[j]>a[i]) {
                int temp=a[i];
                a[i]=a[j];
                a[j]=temp;
                break;
            }
        }
        for (int j=i+1; j<(n+i+1)/2; j++) { //reverse from a[i+1] to a[n-1]
            int temp=a[j];
            a[j]=a[n+i-j];
            a[n+i-j]=temp;
        }
        return true;
    }
    private int[][] ptn;
    private int ssum(int x0, int x1, int y0, int y1) {
        return ptn[x1][y1]
                -(x0>0?ptn[x0-1][y1]:0)
                -(y0>0?ptn[x1][y0-1]:0)
                +(x0>0&&y0>0?ptn[x0-1][y0-1]:0);
    }
    private boolean empty(int x0, int x1, int y0, int y1) {
        return ssum(x0,x1,y0,y1)==0;
    }
    private boolean ig2(int i0, int j0, int i1, int j1) {
        return Math.min(j0,j1)-Math.max(i0,i1)+1>2;
    }
    private boolean[][] tree() {
        List<int[]> rects=new ArrayList<>();
        //r0,r1,c0,c1,depth
        rects.add(new int[] {-1,N,-1,N,0});
        boolean[] master=new boolean[N*N];
        for (int ri=0; ri<rects.size(); ri++) {
            int[] rt=rects.get(ri);
            int br=-1;
            for (int r=rt[0]+2, bscr=Integer.MAX_VALUE; r<rt[1]-1; r++)
                if (empty(r,r,rt[2]+1,rt[3]-1)) {
                    int scr=Math.abs(ssum(rt[0]+1,r-1,rt[2]+1,rt[3]-1)
                            -ssum(r+1,rt[1]-1,rt[2]+1,rt[3]-1));
                    if (scr<bscr) {
                        bscr=scr;
                        br=r;
                    }
                }
            int bc=-1;
            for (int c=rt[2]+2, bscr=Integer.MAX_VALUE; c<rt[3]-1; c++)
                if (empty(rt[0]+1,rt[1]-1,c,c)) {
                    int scr=Math.abs(ssum(rt[0]+1,rt[1]-1,rt[2]+1,c-1)
                            -ssum(rt[0]+1,rt[1]-1,c+1,rt[3]-1));
                    if (scr<bscr) {
                        bscr=scr;
                        bc=c;
                    }
                }
            if ((rt[4]%2==0 || bc==-1) && br!=-1) {
                rects.add(new int[] {rt[0],br,rt[2],rt[3],1-rt[4]});
                rects.add(new int[] {br,rt[1],rt[2],rt[3],1-rt[4]});
                rects.set(ri,null);
                for (int c=rt[2]+1; c<rt[3]; c++) master[br*N+c]=true;
            }
            else if (bc!=-1) {
                rects.add(new int[] {rt[0],rt[1],rt[2],bc,1-rt[4]});
                rects.add(new int[] {rt[0],rt[1],bc,rt[3],1-rt[4]});
                rects.set(ri,null);
                for (int r=rt[0]+1; r<rt[1]; r++) master[r*N+bc]=true;
            }
        }
        {
            List<int[]> tmp=new ArrayList<>();
            for (int[] r:rects) if (r!=null) tmp.add(r);
            rects=tmp;
        }
        int K=rects.size();
        if (K==1) return new boolean[N][N];
        List<List<Integer>> adjs=new ArrayList<>();
        for (int i=0; i<K; i++)
            adjs.add(new ArrayList<>());
        for (int i=0; i<K; i++) {
            int[] r0=rects.get(i);
            for (int j=0; j<i; j++) {
                int[] r1=rects.get(j);
                if (
                        ((r0[0]==r1[1] || r0[1]==r1[0]) && ig2(r0[2],r0[3],r1[2],r1[3]))
                                ||  ((r0[2]==r1[3] || r0[3]==r1[2]) && ig2(r0[0],r0[1],r1[0],r1[1]))
                        ) {
                    adjs.get(i).add(j);
                    adjs.get(j).add(i);
                }
            }
        }
        SplittableRandom rnd=new SplittableRandom(1);
        int[] par=new int[K]; Arrays.fill(par,-1);
        int root=-1;
        {
            boolean[][] startb=new boolean[N][N];
            for (int[] p:starts) startb[p[0]][p[1]]=true;
            for (int i=0; i<K; i++) {
                boolean s=false;
                int[] r$=rects.get(i);
                CHECK: for (int r=r$[0]+1; r<r$[1]; r++)
                    for (int c=r$[2]+1; c<r$[3]; c++)
                        if (startb[r][c]) {
                            s=true;
                            break CHECK;
                        }
                if (s) {
                    root=i;
                    break;
                }
            }
            if (root==-1) throw new RuntimeException();
        }
        {
            List<Integer> verts=new ArrayList<>();
            boolean[] seen=new boolean[K];
            verts.add(root); seen[root]=true;
            for (int oi=0; oi<verts.size(); oi++) {
                int v=verts.get(oi);
                for (int n:adjs.get(v))
                    if (!seen[n]) {
                        seen[n]=true;
                        verts.add(n);
                        par[n]=v;
                    }
            }
        }
        boolean[][] maze=new boolean[N][N];
        for (int i=0; i<N*N; i++) maze[i/N][i%N]=master[i];
        for (int rii=0; rii<rects.size(); rii++)
            if (rii!=root) {
                int[] r0=rects.get(rii), r1=rects.get(par[rii]);
                int cj=Math.min(r0[3],r1[3])-1, ci=Math.max(r0[2],r1[2])+1;
                int rj=Math.min(r0[1],r1[1])-1, ri=Math.max(r0[0],r1[0])+1;
                if (r0[0]==r1[1])
                    maze[r0[0]][rnd.nextInt(cj-ci+1)+ci]=false;
                else if (r0[1]==r1[0])
                    maze[r1[0]][rnd.nextInt(cj-ci+1)+ci]=false;
                else if (r0[2]==r1[3])
                    maze[(rnd.nextInt(rj-ri+1)+ri)][r0[2]]=false;
                else if (r0[3]==r1[2])
                    maze[(rnd.nextInt(rj-ri+1)+ri)][r1[2]]=false;
                else
                    throw new RuntimeException();
            }
        int scr=scr(maze,starts,targets);
        int REPS=0;
        long TIME=System.currentTimeMillis(), TL=END-TIME, TLOG=TL/10, LOG=TIME+TLOG;
        P$.println("K="+K);
        P$.println("TL="+TL);
        P$.printf("%8d%6d%n",REPS,scr);
        boolean[] tmp_maze=new boolean[N*N];
        int[] descs=new int[K], ancs=new int[K];
        int desc_cnt, anc_cnt;
        while (TIME<END) {
            int ri=rnd.nextInt(K-1); if (ri>=root) ri++;
            {
                int[] r0=rects.get(ri), r1=rects.get(par[ri]);
                int cj=Math.min(r0[3],r1[3])-1, ci=Math.max(r0[2],r1[2])+1;
                int rhi=Math.min(r0[1],r1[1])-1, rlo=Math.max(r0[0],r1[0])+1;
                if (r0[0]==r1[1])
                    for (int c=ci; c<=cj; c++) {
                        tmp_maze[r0[0]*N+c]=maze[r0[0]][c];
                        maze[r0[0]][c]=true;
                    }
                else if (r0[1]==r1[0])
                    for (int c=ci; c<=cj; c++) {
                        tmp_maze[r0[1]*N+c]=maze[r0[1]][c];
                        maze[r0[1]][c]=true;
                    }
                else if (r0[2]==r1[3])
                    for (int r=rlo; r<=rhi; r++) {
                        tmp_maze[r*N+r0[2]]=maze[r][r0[2]];
                        maze[r][r0[2]]=true;
                    }
                else if (r0[3]==r1[2])
                    for (int r=rlo; r<=rhi; r++) {
                        tmp_maze[r*N+r0[3]]=maze[r][r0[3]];
                        maze[r][r0[3]]=true;
                    }
                else
                    throw new RuntimeException();
            }
            //P$.println("ri="+ri);
            //remove edge ri--par[ri]
            desc_cnt=0;
            descs[desc_cnt++]=ri;
            for (int oi=0; oi<desc_cnt; oi++) {
                int v=descs[oi];
                //P$.print("v="+v+":");
                for (int n:adjs.get(v))
                    if (par[n]==v) {
                        descs[desc_cnt++]=n;
                        //P$.print(" "+n);
                    }
                //P$.println();
            }
            //P$.println("descendants="+descendants);
            boolean[] desc=new boolean[K]; for (int i=0; i<desc_cnt; i++) desc[descs[i]]=true;
            List<int[]> edges=new ArrayList<>();
            for (int oi=0; oi<desc_cnt; oi++) {
                int i=descs[oi];
                for (int n:adjs.get(i))
                    if (!desc[n] && par[n]!=i && par[i]!=n)
                        edges.add(new int[] {i,n});
            }
            if (edges.size()==0)
                continue;
            //for (int[] e:edges) P$.print(" "+Arrays.toString(e)); P$.println();
            int[] edge=edges.get(rnd.nextInt(edges.size()));
            int co;
            {
                int[] r0=rects.get(edge[0]), r1=rects.get(edge[1]);
                int cj=Math.min(r0[3],r1[3])-1, ci=Math.max(r0[2],r1[2])+1;
                int rhi=Math.min(r0[1],r1[1])-1, rlo=Math.max(r0[0],r1[0])+1;
                if (r0[0]==r1[1]) {
                    co=rnd.nextInt(cj-ci+1)+ci;
                    maze[r0[0]][co]=false;
                }
                else if (r0[1]==r1[0]) {
                    co=rnd.nextInt(cj-ci+1)+ci;
                    maze[r1[0]][co]=false;
                }
                else if (r0[2]==r1[3]) {
                    co=rnd.nextInt(rhi-rlo+1)+rlo;
                    maze[co][r0[2]]=false;
                }
                else if (r0[3]==r1[2]) {
                    co=rnd.nextInt(rhi-rlo+1)+rlo;
                    maze[co][r1[2]]=false;
                }
                else
                    throw new RuntimeException();
            }
            //P$.println("edge="+Arrays.toString(edge));
            //TODO: LEAVE par[] MODIFICATION TO THE if (nscr>=ubound) {} CLAUSE
            int nscr=scr(maze,starts,targets);
            if (nscr>=scr) {
                scr=nscr;
                //P$.println(ubound);
                anc_cnt=0;
                for (int i=edge[0]; i!=-1; i=(i==ri?-1:par[i])) ancs[anc_cnt++]=i;
                //P$.println("ancs="+ancs);
                par[ri]=-1;
                par[edge[0]]=edge[1];
                for (int i=1; i<anc_cnt; i++) par[ancs[i]]=ancs[i-1];
            }
            else {
                {
                    int[] r0=rects.get(edge[0]), r1=rects.get(edge[1]);
                    if (r0[0]==r1[1])
                        maze[r0[0]][co]=true;
                    else if (r0[1]==r1[0])
                        maze[r1[0]][co]=true;
                    else if (r0[2]==r1[3])
                        maze[co][r0[2]]=true;
                    else if (r0[3]==r1[2])
                        maze[co][r1[2]]=true;
                    else
                        throw new RuntimeException();
                }
                {
                    int[] r0=rects.get(ri), r1=rects.get(par[ri]);
                    int cj=Math.min(r0[3],r1[3])-1, ci=Math.max(r0[2],r1[2])+1;
                    int rhi=Math.min(r0[1],r1[1])-1, rlo=Math.max(r0[0],r1[0])+1;
                    if (r0[0]==r1[1])
                        for (int c=ci; c<=cj; c++)
                            maze[r0[0]][c]=tmp_maze[r0[0]*N+c];
                    else if (r0[1]==r1[0])
                        for (int c=ci; c<=cj; c++)
                            maze[r0[1]][c]=tmp_maze[r0[1]*N+c];
                    else if (r0[2]==r1[3])
                        for (int r=rlo; r<=rhi; r++)
                            maze[r][r0[2]]=tmp_maze[r*N+r0[2]];
                    else if (r0[3]==r1[2])
                        for (int r=rlo; r<=rhi; r++)
                            maze[r][r0[3]]=tmp_maze[r*N+r0[3]];
                    else
                        throw new RuntimeException();
                }
            }
            REPS++;
            TIME=System.currentTimeMillis();
            if (TIME>LOG) {
                LOG+=TLOG;
                P$.printf("%8d%6d%n",REPS,scr);
            }
        }
        P$.printf("%8d%6d%n",REPS,scr);
        return maze;
    }

    private static boolean[][] dsnake(boolean[][] taken) {
        //TODO: greedyily add one wall cell at a time at end of algorithm
        //TODO: give slight improvements to dsnake()
        int M=taken.length, N=taken[0].length;
        class $ {
            public boolean in(int r, int c) {
                return r>-1 && r<M && c>-1 && c<N;
            }
            public boolean in(int[] p) {
                return in(p[0],p[1]);
            }
        } $ $=new $();
        boolean[][] out=new boolean[M][N];
        for (int d=0, dir=0; d<M+N-2;) {
            int[] p;
            if (dir==0) {
                p=new int[] {d,0};
                if (!$.in(p)) p=new int[] {M-1,d-(M-1)};
            }
            else {
                p=new int[] {0,d};
                if (!$.in(p)) p=new int[] {d-(N-1),N-1};
            }
            boolean e=true;
            for (int r=Math.min(M-1,d), c=d-r; r>=0 && c<=N-1 && e; r--, c++)
                if ($.in(r,c) && (r!=p[0] || c!=p[1]) && taken[r][c])
                    e=false;
            if (e) {
                for (int r=Math.min(M-1,d), c=d-r; r>=0 && c<=N-1; r--, c++)
                    if ($.in(r,c))
                        out[r][c]=true;
                out[p[0]][p[1]]=false;
                d+=dir==1&&M==3?2:3;
                dir=1-dir;
            }
            else
                d++;
        }
        return out;
    }
    private boolean[][] snake(boolean[][] taken, int[] slot, int rdir) {
        boolean[][] out=new boolean[N][N];
        for (int r=0, dir=rdir; r<N; r++)
            if (slot[r]!=-1) {
                boolean e=true;
                for (int c=dir; c<N-1+dir && e; c++) if (taken[r][c]) e=false;
                if (e) {
                    for (int c=dir; c<N-1+dir; c++)
                        out[r][c]=true;
                    dir=1-dir;
                }
                else
                    for (int c=0; c<N; c++)
                        out[r][c]=!taken[r][c];
            }
        for (int i$=-1; i$<N;)
            if (i$!=-1 && slot[i$]==-1)
                i$++;
            else {
                int j$=i$+1; while (j$<N && slot[j$]==-1) j$++;
                if (j$-i$==3) {
                    for (int c=1, d=slot[j$]%2; c<N-1;)
                        if (!taken[i$+1+d][c]) {
                            out[i$+1+d][c]=true;
                            d=1-d;
                            c+=2;
                        }
                        else
                            c++;
                }
                if (j$-i$>3) {
                    boolean[][] ds=itranss(slot[j$],dsnake(transs(slot[j$],smat(taken,i$+1,j$-1,0,N-1))));
                    if (ds.length!=(j$-1)-(i$+1)+1 || ds[0].length!=N)
                        throw new RuntimeException("Should be "+((j$-1)-(i$+1)+1)+"x"+N
                                +", actually "+ds.length+"x"+ds[0].length);
                    paste(out,ds,i$+1,0);
                }
                i$=j$;
            }
        return out;
    }
    private boolean[][] bsnake(boolean[][] taken_arg, int rdir, int bits) {
        boolean[][] taken=transs(bits,taken_arg);
        int[] slot=new int[N+1]; Arrays.fill(slot,-1);
        int[] tmpslot=new int[N+1];
        boolean[][] bmaze=null;
        int bscr=Integer.MIN_VALUE;
        {
            int bsl=-1;
            for (int sl=0; sl<8; sl++) {
                slot[N]=sl;
                boolean[][] ns=snake(taken,slot,rdir);
                int nscr=scr(itranss(bits,ns),starts,targets);
                if (nscr>bscr) {
                    bmaze=ns;
                    bscr=nscr;
                    bsl=sl;
                }
            }
            slot[N]=bsl;
        }
        P$.println("["+N+"]="+slot[N]+" --> "+bscr);
        PROC: while (true) {
            int bsi=-1;
            int bsl=-1;
            for (int si=0; si<N; si++)
                if (System.currentTimeMillis()>END)
                    break PROC;
                else if (slot[si]==-1) {
                    int lsi=Math.max(0,si-2), hsi=Math.min(N-1,si+2);
                    System.arraycopy(slot,lsi,tmpslot,lsi,hsi-lsi+1);
                    for (int i=lsi; i<=hsi; i++)
                        if (i!=si) slot[i]=-1;
                    for (int sl=0; sl<8; sl++) {
                        slot[si]=sl;
                        boolean[][] ns=snake(taken,slot,rdir);
                        int nscr=scr(itranss(bits,ns),starts,targets);
                        if (nscr>bscr) {
                            bmaze=ns;
                            bscr=nscr;
                            bsi=si;
                            bsl=sl;
                        }
                    }
                    System.arraycopy(tmpslot,lsi,slot,lsi,hsi-lsi+1);
                }
            if (bsi==-1) break;
            P$.println("["+bsi+"]="+bsl+" --> "+bscr);
            slot[bsi]=bsl;
            for (int i=Math.max(0,bsi-2); i<=Math.min(N-1,bsi+2); i++)
                if (i!=bsi) slot[i]=-1;
        }
        return itranss(bits,bmaze);
    }
    private boolean[][] best(boolean[][] a, boolean[][] b) {
        return scr(a,starts,targets)>=scr(b,starts,targets)?a:b;
    }
    public char[] findSolution(int N, int R, int T, int[][] starts, int[][][] targets) {
        END=System.currentTimeMillis()+9500;
        this.N=N; this.R=R; this.T=T;
        this.starts=starts;
        this.targets=targets;
        boolean[][] taken=new boolean[N][N];
        ptn=new int[N][N];
        for (int r=0; r<R; r++) {
            taken[starts[r][0]][starts[r][1]]=true;
            ptn[starts[r][0]][starts[r][1]]++;
            for (int t=0; t<T; t++) {
                taken[targets[r][t][0]][targets[r][t][1]]=true;
                ptn[targets[r][t][0]][targets[r][t][1]]++;
            }
        }
        for (int r=0; r<N; r++)
            for (int c=0; c<N; c++)
                ptn[r][c]+=(r>0?ptn[r-1][c]:0)
                        +(c>0?ptn[r][c-1]:0)
                        -(r>0&&c>0?ptn[r-1][c-1]:0);
        boolean[][] bmaze=null;
        int bscr=0;
        for (int bits=0; bits<8; bits++) {
            boolean[][] s=itranss(bits,dsnake(transs(bits,taken)));
            int scr=scr(s,starts,targets);
            if (scr>bscr) {
                bscr=scr;
                bmaze=s;
            }
        }
        P$.println("dsnakeD8 --> "+bscr);
        for (int type=0; type<4; type++) {
            P$.println("islot "+type);
            bmaze=best(bmaze,bsnake(taken,type&1,type&2));
        }
        {
            bscr=scr(bmaze,starts,targets);
            P$.println("improve\n"+bscr);
            long ITIME=System.currentTimeMillis();
            G1: for (int i=0; i<N; i++)
                for (int j=0; j<N; j++)
                    if (System.currentTimeMillis()>=END)
                        break G1;
                    else if (!bmaze[i][j]) {
                        bmaze[i][j]=true;
                        int nscr=scr(bmaze,starts,targets);
                        if (nscr>bscr) {
                            bscr=nscr;
                            P$.println("("+i+","+j+") --> "+nscr);
                        }
                        else
                            bmaze[i][j]=false;
                    }
            P$.println("improve time="+(System.currentTimeMillis()-ITIME));
        }
        P$.println("tree");
        boolean[][] tree=tree();
        int tscr=scr(tree,starts,targets);
        if (tscr>scr(bmaze,starts,targets)) {
            bmaze=tree;
            bscr=tscr;
            P$.println("improve\n"+bscr);
            long ITIME=System.currentTimeMillis();
            G1: for (int i=0; i<N; i++)
                for (int j=0; j<N; j++)
                    if (System.currentTimeMillis()>=END+100)
                        break G1;
                    else if (!bmaze[i][j]) {
                        bmaze[i][j]=true;
                        int nscr=scr(bmaze,starts,targets);
                        if (nscr>bscr) {
                            bscr=nscr;
                            P$.println("("+i+","+j+") --> "+nscr);
                        }
                        else
                            bmaze[i][j]=false;
                    }
            P$.println("improve time="+(System.currentTimeMillis()-ITIME));
        }
        char[] ret=new char[N*N];
        for (int i=0; i<N*N; i++) ret[i]=bmaze[i/N][i%N]?'#':'.';
        return ret;
    }
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            int N = Integer.parseInt(br.readLine());
            int R = Integer.parseInt(br.readLine());
            int T = Integer.parseInt(br.readLine());
            int[][] Starts=new int[R][2];
            int[][][] Targets=new int[R][T][2];
            for (int i=0; i<R; i++) {
                String[] temp = br.readLine().split(" ");
                Starts[i][0]=Integer.parseInt(temp[0]);
                Starts[i][1]=Integer.parseInt(temp[1]);
                for (int k=0; k<T; k++) {
                    String[] temp2 = br.readLine().split(" ");
                    Targets[i][k][0]=Integer.parseInt(temp2[0]);
                    Targets[i][k][1]=Integer.parseInt(temp2[1]);
                }
            }
            HardestMaze prog = new HardestMaze();
            char[] ret = prog.findSolution(N, R, T, Starts, Targets);
            System.out.println(ret.length);
            for (int i = 0; i < ret.length; i++)
                System.out.println(ret[i]);
        }
        catch (Exception e) {}
    }
}