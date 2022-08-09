import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.*;
public class ImageFromBlocks {
    private int IH, IW, BS, MD, Dleft;
    private double EXPSCR;
    private boolean recent;
    private int[] image, piles;
    private double[][] bestscrs;
    HashMap<Integer,int[][][]> endoffs;
    HashMap<Integer,int[][]> rposs;
    SplittableRandom rnd;
    public String init(int imageHeight, int blockSize, int maxDiscard, int[] image) {
        IH =imageHeight;
        IW =image.length / imageHeight;
        BS=blockSize;
        MD=maxDiscard;
        Dleft=MD;
        this.image=image.clone();
        piles=new int[IW /BS];
        for (int i=0; i<piles.length; i++) piles[i]=IH/BS;
        bestscrs=new double[IH/BS][IW/BS];
        for (int i = 0; i < IH / BS; i++) {
            for (int j = 0; j < IW / BS; j++) {
                int s = BS * BS;
                int[] r = new int[s], g = new int[s], b = new int[s];
                for (int id = 0, ir = BS * i; ir < BS * (i + 1); ir++)
                    for (int ic = BS * j; ic < BS * (j + 1); ic++) {
                        int col = image[ir * IW + ic];
                        r[id] = (col >> 16) & 0xff;
                        g[id] = (col >> 8) & 0xff;
                        b[id] = col & 0xff;
                        id++;
                    }
                double err = mindist(r) + mindist(g) + mindist(b);
                err /= s * 3 * 255;
                bestscrs[i][j]=(1 - err) * (1 - err);
            }
        }
        rnd=new SplittableRandom(1);
        endoffs=new HashMap<>();
        rposs=new HashMap<>();
        for (int[][] pc:pieces) {
            int code=code(pc,0);
            addOffs(pc,code,0);
            addRposs(code);
            /*System.out.println(code);
            for (int i=0; i<4; i++)
                System.out.println(Arrays.toString(rposs.get(code)[i])+" "+Arrays.toString(endoffs.get(code)[i][1]));*/
        }
        setEXPSCR();
        return "";
    }
    private int random(int min, int max) {
        return rnd.nextInt(max - min + 1) + min;
    }
    private static int mindist(int[] a) {
        int[] sorted=a.clone();
        Arrays.sort(sorted);
        int med=sorted[sorted.length/2];
        int out=0;
        for (int i=0; i<sorted.length; i++)
            out+=Math.abs(sorted[i]-med);
        return out;
    }
    private static int R(int c) {
        return (c>>16)&0xff;
    }
    private static int G(int c) {
        return (c>>8)&0xff;
    }
    private static int B(int c) {
        return c&0xff;
    }
    private int diff(int c0, int c1) {
        return Math.abs(R(c0)-R(c1))
                +Math.abs(G(c0)-G(c1))
                +Math.abs(B(c0)-B(c1));
    }
    private double scr(int x, int y, int col) {
        double out=0;
        for (int ir = x; ir< IH && ir<x+BS; ir++)
            for (int ic = y; ic< IW && ic<y+BS; ic++)
                out+=diff(image[ir* IW +ic],col);
        out/=(double)BS*BS*3*255;
        out=1-out;
        out*=out;
        return out;
    }
    private static int clen(int[][] pc, int rot) {
        return rot%2==0?pc[0].length:pc.length;
    }
    private static int rlen(int[][] pc, int rot) {
        return rot%2==0?pc.length:pc[0].length;
    }
    private static int val(int[][] pc, int rot, int r, int c) {
        //value of pc'[r][c], where pc'=pc rotated rot times 90 deg CW
        if (rot==0)
            return pc[r][c];
        if (rot==1)
            return pc[pc.length-1-c][r];
        if (rot==2)
            return pc[pc.length-1-r][pc[0].length-1-c];
        return pc[c][pc[0].length-1-r];
    }
    private int[][] endoffs(int[][] pc, int rot,int blank) {
        int clen=clen(pc,rot);
        int[] clmoffs=new int[clen], preoffs=new int[clen];
        for (int c=0; c<clen; c++) {
            int r=rlen(pc,rot)-1;
            while (val(pc,rot,r,c)==blank)
                r--;
            clmoffs[c]=r;
            r=0;
            while (val(pc,rot,r,c)==blank)
                r++;
            preoffs[c]=r;
        }
        return new int[][] {preoffs, clmoffs};
    }
    /*private void setRpos(int pos) {
        for
    }*/
    private int rpos(int[] sufoffs, int pos) { //row pos of piece after falling down
        int rpos=Integer.MAX_VALUE;
        for (int clm=0; clm<sufoffs.length; clm++) {
            int trp=piles[pos+clm]-1-sufoffs[clm];
            if (trp<rpos) rpos=trp;
        }
        return rpos;
    }
    private double[] bplace(int[][] pc, int code, int rot) {
        int[][] offs=endoffs.get(code)[rot];
        int bpos=-1;
        double bscr=Double.NEGATIVE_INFINITY, bsscr=-1;
        int brpos=-1;
        int clen=clen(pc,rot);
        int rlen=rlen(pc,rot);
        for (int pos = 0; pos<=IW/BS-clen; pos++) {
            int rpos=rposs.get(code)[rot][pos];
            if (rpos<0) continue;
            double sscr=0;
            for (int r=0; r<rlen; r++)
                for (int c=0; c<clen; c++)
                    if (val(pc,rot,r,c) > -1)
                        sscr += scr((rpos + r) * BS, (pos + c) * BS, val(pc,rot,r,c));
            double scr=sscr;
            for (int c=0; c<clen; c++)
                for (int r=offs[0][c]; rpos + r<piles[pos+c]; r++)
                    scr-=bestscrs[rpos+r][pos+c];
            if (clen==1) {
                int p=piles[pos];
                int depth=Math.max(0,p-(pos>0?piles[pos-1]:IH/BS));
                depth=Math.min(depth,Math.max(0,p-(pos+1<piles.length?piles[pos+1]:IH/BS)));
                for (int d=0; d<Math.min(rlen,depth); d++)
                    scr+=bestscrs[rpos+rlen-1-d][pos];
            }
            if (scr>bscr) {
                bscr=scr;
                bpos=pos;
                bsscr=sscr;
                brpos=rpos;
            }
        }
        return new double[] {bpos,brpos,bscr,bsscr};
    }
    private double[] bplacement(int[][] pc, int code) {
        int bpos=-1, brot=0, brpos=-1;
        double bscr=Double.NEGATIVE_INFINITY, bsscr=-1;
        for (int rot=0; rot<4; rot++) {
            //System.out.println(IW/BS-clen(pc,rot));
            double[] info=bplace(pc, code, rot);
            double scr=info[2];
            if (scr>bscr) {
                bscr=scr;
                brot=rot;
                bpos=(int)info[0];
                brpos=(int)info[1];
                bsscr=info[3];
            }
        }
        return new double[] {bpos, brpos, brot, bscr, bsscr};
    }
    private void addOffs(int[][] pc, int code,int blank) {
        if (!endoffs.keySet().contains(code)) {
            int[][][] coll=new int[4][][];
            for (int rot=0; rot<4; rot++)
                coll[rot]=endoffs(pc,rot,blank);
            endoffs.put(code,coll);
        }
    }
    private void addRposs(int code) {
        if (!rposs.keySet().contains(code)) {
            int[][] ss=new int[4][];
            for (int i=0; i<4; i++) {
                ss[i]=new int[IW/BS+1-endoffs.get(code)[i][1].length];
                for (int r = 0; r < ss[i].length; r++)
                    ss[i][r] = rpos(endoffs.get(code)[i][1], r);
            }
            rposs.put(code,ss);
        }
    }
    private int code(int[][] pc, int blank) {
        int code=0;
        for (int i=0; i<pc.length; i++) {
            code *= 3;
            code += 2;
            for (int j = 0; j < pc[i].length; j++) {
                code *= 3;
                if (pc[i][j] != blank)
                    code++;
            }
        }
        return code;
    }
    private void setEXPSCR() {
        EXPSCR=0;
        int TRIALS=50;
        for (int rep=0; rep<TRIALS; rep++) {
            //taken from tester
            int[][] rndpc=rndPiece();
            int code=code(rndpc,-1);
            addOffs(rndpc,code,-1);
            double[] info=bplacement(rndpc,code);
            int bpos=(int)info[0], brpos=(int)info[1], brot=(int)info[2];
            if (bpos==-1) continue;
            int[][] offs=endoffs.get(code)[brot];
            double bpurity=0;
            for (int c=0; c<offs[0].length; c++)
                for (int r = offs[0][c]; brpos + r < piles[bpos + c]; r++)
                    bpurity += bestscrs[brpos + r][bpos + c];
            EXPSCR+=info[4]/bpurity;
        }
        EXPSCR/=TRIALS;
        recent=true;
    }
    public String placePiece(int pH, int[] piecearg, int time) {
        int pW=piecearg.length/pH;
        int[][] pc=new int[pH][pW];
        for (int i=0; i<piecearg.length; i++)
            pc[i / pW][i % pW] = piecearg[i];
        int pccode=code(pc,-1);
        double[] info=bplacement(pc,pccode);
        int bpos=(int)info[0], brpos=(int)info[1], brot=(int)info[2];
        if (bpos==-1) {
            if (Dleft>0) {
                Dleft--;
                return "D";
            }
            else return "0 -1";
        }
        int[][] offs=endoffs.get(pccode)[brot];
        if (Dleft>0) {
            double bpurity=0;
            for (int c=0; c<offs[0].length; c++)
                for (int r = offs[0][c]; brpos + r < piles[bpos + c]; r++)
                    bpurity += bestscrs[brpos + r][bpos + c];
            /*double cutoff=1-1.0/(2.5*Dleft/MD+1.0);
            boolean bad=info[4]/bpurity<cutoff;
            //System.out.print(cutoff);
            if (bad) {
                if (!recent) setEXPSCR();
                bad=info[4]/bpurity<cutoff*EXPSCR;
                //System.out.print(" "+cutoff*EXPSCR);
            }*/
            double cutoff=1-1.0/(1.0*Dleft/MD+0.1);
            cutoff-=1;
            boolean bad=info[4]-bpurity<cutoff;
            //System.out.println(info[4]-bpurity+" "+cutoff);
            //System.out.println();
            if (bad) {
                Dleft--;
                return "D";
            }
        }
        for (int c=0; c<offs[0].length; c++)
            piles[bpos+c]=brpos+offs[0][c];
        for (int code:rposs.keySet()) {
            int w=endoffs.get(code)[0][0].length;
            for (int rot=0; rot<4; rot++)
                for (int c = Math.max(0,bpos-w); c < Math.min(rposs.get(code)[rot].length,bpos+offs[0].length); c++)
                    rposs.get(code)[rot][c]=rpos(endoffs.get(code)[rot][1],c);
        }
        recent=false;
        return brot+" "+bpos;
    }
    //taken from tester
    private static final int[][][] pieces = new int[][][] {
            {{1,1}},
            {{1,1,1}},
            {{1,0},{1,1}},
            {{1,1,1,1}},
            {{1,1},{1,1}},
            {{0,1,0},{1,1,1}},
            {{1,1,0},{0,1,1}},
            {{0,1,1},{1,1,0}},
            {{1,0,0},{1,1,1}},
            {{0,0,1},{1,1,1}}
    };
    private int[][] rndPiece() {
        int currPieceIdx = rnd.nextInt(pieces.length);
        int[][] basePiece = pieces[currPieceIdx];
        int[][] currPiece = new int[basePiece.length][basePiece[0].length];
        int xBase = rnd.nextInt(IW);
        int yBase = rnd.nextInt(IH);
        for (int i = 0; i < basePiece.length; i++) {
            for (int j = 0; j < basePiece[0].length; j++) {
                if (basePiece[i][j] == 0) {
                    currPiece[i][j] = -1;
                } else {
                    int xp = random(Math.max(0, xBase - BS), Math.min(IW - 1, xBase + BS));
                    int yp = random(Math.max(0, yBase - BS), Math.min(IH - 1, yBase + BS));
                    currPiece[i][j] = image[yp * IW + xp];
                }
            }
        }
        return currPiece;
    }
    /*-------8<------- end of solution submitted to the website -------8<------- */
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            ImageFromBlocks sol = new ImageFromBlocks();

            int imageHeight = Integer.parseInt(br.readLine());
            int blockSize = Integer.parseInt(br.readLine());
            int maxDiscard = Integer.parseInt(br.readLine());
            int imageLength = Integer.parseInt(br.readLine());
            int[] image = new int[imageLength];
            for (int i = 0; i < image.length; i++) {
                image[i] = Integer.parseInt(br.readLine());
            }

            String retInit = sol.init(imageHeight, blockSize, maxDiscard, image);
            System.out.println(retInit);
            System.out.flush();

            while (true) {
                int pieceHeight = Integer.parseInt(br.readLine());
                int pieceLength = Integer.parseInt(br.readLine());
                int[] piece = new int[pieceLength];
                for (int i = 0; i < piece.length; ++i) {
                    piece[i] = Integer.parseInt(br.readLine());
                }
                int timeLeft = Integer.parseInt(br.readLine());
                String retPlacePiece = sol.placePiece(pieceHeight, piece, timeLeft);
                System.out.println(retPlacePiece);
                System.out.flush();
            }
        } catch (Exception e) {
        }
    }
}