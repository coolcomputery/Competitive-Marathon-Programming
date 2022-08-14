import java.io.*;
import java.util.*;
//https://www.topcoder.com/challenges/30103195
public class JumpAround {
    private static final int[] dr={ 0,1,0,-1};
    private static final int[] dc={-1,0,1, 0};
    private static final char[] names={'L','D','R','U'};
    private int N;
    private int inv(int dir) {
        return (dir/4)*4+((dir%4+2)%4);
    }
    private boolean obs(char c) {
        return c=='P' || c=='#';
    }
    private Path dijkstra(Collection<Integer> starts, Collection<Integer> targets, char[] grid) {
        int[] cost=new int[N*N];
        int[] dir=new int[N*N];
        for (int i=0; i<cost.length; i++) {
            cost[i] = Integer.MAX_VALUE;
            dir[i] = -1;
        }
        for (int si:starts)
            cost[si]=0;
        PriorityQueue<Integer> fronts=new PriorityQueue<>(1,
                new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return cost[o1]-cost[o2];
                    }
                }
        );
        fronts.addAll(starts);
        while (fronts.size()>0) {
            int best=fronts.poll();
            if (targets.contains(best)) {
                //path found
                return new Path(dir,best);
            }
            //slide
            for (int d=0; d<dr.length; d++) {
                int r=best/N+dr[d], c=best%N+dc[d];
                if (r<0 || r>=N || c<0 || c>=N) continue;
                if (obs(grid[r*N+c])) continue;
                int ncost=cost[best]+1;
                if (ncost<cost[r*N+c]) {
                    cost[r*N+c]=ncost;
                    dir[r*N+c]=d;
                    fronts.add(r*N+c);
                }
            }
            //jump
            for (int d=0; d<dr.length; d++) {
                int sr=best/N+dr[d], sc=best%N+dc[d];
                int r=best/N+2*dr[d], c=best%N+2*dc[d];
                if (r<0 || r>=N || c<0 || c>=N) continue;
                if (obs(grid[r*N+c])) continue;
                if (!obs(grid[sr*N+sc])) continue;
                int ncost=cost[best]+(dir[best]<4?1:0);
                if (ncost<cost[r*N+c]) {
                    cost[r*N+c]=ncost;
                    dir[r*N+c]=d+4;
                    fronts.add(r*N+c);
                }
            }
        }
        return new Path(-1,-1,new ArrayList<>());
    }
    public String[] findSolution(int N, int P, char[] orig_grid) {
        long END=System.currentTimeMillis()+9800;
        StringBuilder log=new StringBuilder();
        log.append("P="+P+"\n");
        this.N=N;
        char[] grid=orig_grid.clone();
        HashSet<Integer> to_move=new HashSet<>(), to_fill=new HashSet<>();
        for (int r=0; r<N; r++)
            for (int c=0; c<N; c++)
                if (grid[r*N+c]=='P')
                    to_move.add(r * N + c);
                else if (grid[r*N+c]=='X')
                    to_fill.add(r*N+c);
        ArrayList<Integer> STARTS=new ArrayList<>(), targets=new ArrayList<>();
        ArrayList<Path> paths=new ArrayList<>();
        int scr=2*N*P;
        while(to_move.size()>0) {
            Path path=dijkstra(to_move,to_fill,grid);
            if (path.start==-1) break;
            grid[path.start]='.';
            grid[path.end]='#';
            to_move.remove(path.start);
            to_fill.remove(path.end);
            STARTS.add(path.start);
            targets.add(path.end);
            scr-=2*N;
            scr+=path.moves.size();
            paths.add(path);
        }
        STARTS.addAll(to_move);
        targets.addAll(to_fill);
        while (paths.size()<P)
            paths.add(new Path(-1,-1,new ArrayList<>()));
        log.append("scr "+scr);
        SplittableRandom rnd=new SplittableRandom(1);
        int REPS=0;
        int[] ACCN=new int[3];
        boolean large=P>250;
        while (System.currentTimeMillis()<END) {
            int pi=rnd.nextInt(P), pj=rnd.nextInt(P-1);
            if (pj>=pi)
                pj++;
            int ti=targets.get(pi), tj=targets.get(pj);
            int si=STARTS.get(pi), sj=STARTS.get(pj);
            int mode=large?(rnd.nextDouble()<0.9?3:(rnd.nextInt(2)+1)):(rnd.nextInt(3)+1);
            if ((mode&1)>0) {
                targets.set(pi, tj);
                targets.set(pj, ti);
            }
            if ((mode&2)>0) {
                STARTS.set(pi,sj);
                STARTS.set(pj,si);
            }
            ArrayList<Path> npaths=new ArrayList<>();
            grid=orig_grid.clone();
            int nscr=2*N*P;
            for (int i=0; i<P; i++) {
                Path path;
                if (i<Math.min(pi,pj) || i>Math.max(pi,pj))
                    path=paths.get(i);
                else {
                    HashSet<Integer> s = new HashSet<>(), t = new HashSet<>();
                    s.add(STARTS.get(i));
                    t.add(targets.get(i));
                    path = dijkstra(s, t, grid);
                }
                npaths.add(path);
                if (path.start==-1)
                    continue;
                grid[path.start]='.';
                grid[path.end]='#';
                nscr+=path.moves.size()-2*N;
            }
            if (nscr<=scr) {
                scr=nscr;
                paths=npaths;
                ACCN[mode-1]++;
            }
            else {
                if ((mode&1)>0) {
                    targets.set(pi, ti);
                    targets.set(pj, tj);
                }
                if ((mode&2)>0) {
                    STARTS.set(pi,si);
                    STARTS.set(pj,sj);
                }
            }
            REPS++;
        }
        log.append("->"+scr+"\nreps="+REPS+"\n");
        log.append(Arrays.toString(ACCN)+"\n");
        //log.append(Arrays.toString(freq)+"\n");
        System.err.print(log);
        ArrayList<Move> moves=new ArrayList<>();
        for (Path p:paths)
            moves.addAll(p.moves);
        String[] out=new String[moves.size()];
        for (int i=0; i<moves.size(); i++)
            out[i]=moves.get(i).toString();
        return out;
    }
    class Path {
        ArrayList<Move> moves;
        int start, end;
        public Path(int start, int end, ArrayList<Move> moves) {
            this.start=start;
            this.end=end;
            this.moves=moves;
        }
        public Path(int[] dir, int best) {
            ArrayList<Move> moves=new ArrayList<>(), out=new ArrayList<>();
            int start=-1;
            String jumpDirs="";
            for (int vert=best; dir[vert]!=-1;) {
                int inv=inv(dir[vert]);
                if (dir[vert]<4) {
                    int prev = (vert / N + dr[inv]) * N + (vert % N + dc[inv]);
                    moves.add(new Move(prev / N, prev % N, dir[vert]));
                    vert = prev;
                }
                else {
                    inv-=4;
                    int prev = (vert / N + 2*dr[inv]) * N + (vert % N + 2*dc[inv]);
                    jumpDirs=names[dir[vert]-4]+jumpDirs;
                    vert=prev;
                }
                if (dir[vert]<4 && jumpDirs.length()>0) {
                    moves.add(new Move(vert/N,vert%N,true,jumpDirs));
                    jumpDirs="";
                }
                if (dir[vert]==-1)
                    start=vert;
            }
            for (int i=moves.size()-1; i>-1; i--)
                out.add(moves.get(i));
            this.start=start;
            this.end=best;
            this.moves=out;
        }
    }
    static class Move {
        int r, c;
        boolean jump;
        String dirs;
        public Move(int r, int c, boolean j, String d) {
            this.r=r;
            this.c=c;
            jump=j;
            if (!j)
                assert(d.length()==1);
            dirs=d;
        }
        public Move(int r, int c, int dir) {
            this(r,c,false,names[dir]+"");
        }
        public String toString() {
            return r+" "+c+" "+(jump?"J":"S")+" "+dirs;
        }
    }
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            int N = Integer.parseInt(br.readLine());
            int NumPegs = Integer.parseInt(br.readLine());
            char[] grid = new char[N*N];
            for (int i=0; i<N*N; i++) grid[i]=br.readLine().charAt(0);
            JumpAround jp = new JumpAround();
            String[] ret = jp.findSolution(N, NumPegs, grid);
            System.out.println(ret.length);
            for (int i = 0; i < ret.length; i++) System.out.println(ret[i]);
            System.out.flush();
        }
        catch (Exception e) {}
    }
}