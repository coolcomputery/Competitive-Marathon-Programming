import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
public class TypesetterArt {
    private int nRenders, nFonts, precision, WIDTH, HEIGHT;
    private int[][][] imgLayers; //original image
    private int[][][] newLayers; //built image
    private double[] layerScores; //scores for ea. color space
    private boolean[] layerOpen;
    private int fontId, letterId, fontSize;
    private int firstCode;
    /**
     * collects all chars of certain sizes of one font
     */
    private HashMap<Integer, int[][][]> letters; //font size, char ASCII id-32, pixels[][]
    private long glyphStart, glyphEnd, renderStart, renderEnd;
    public int init(int nRenders, int nFonts, int precision, int width, int height, int[] image) {
        this.nRenders=nRenders;
        this.nFonts=nFonts;
        this.precision=precision;
        WIDTH=height; //<- yes, this is actually correct
        HEIGHT=width;
        imgLayers=new int[4][WIDTH][HEIGHT];
        newLayers=new int[4][WIDTH][HEIGHT];
        for (int i=0; i<4; i++) {
            for (int x=0; x<WIDTH; x++) {
                for (int y=0; y<HEIGHT; y++) {
                    imgLayers[i][x][y]=image[WIDTH*HEIGHT*i+HEIGHT*x+y];
                    newLayers[i][x][y]=0;
                }
            }
        }
        firstCode=0;
        fontId=0;
        letterId=31;
        fontSize=8;
        letters=new HashMap<>();
        letters.put(fontSize, new int[127-32][][]);
        System.out.println("IMAGE DIM: "+WIDTH+"x"+HEIGHT+" w/ nRenders="+nRenders+", prec="+precision);
        return 0;
    }
    private static int[][] processedBitmask(int width, int height, int[] bitmask) {
        int[][] pixels=new int[height][width]; //actually correct
        int rowLength = (width + 31) / 32;
        for (int k = 0; k < bitmask.length; k++) {
            int x = bitmask[k];
            int left = (k % rowLength) * 32;
            int row = k / rowLength;
            for (int m = 0; m < 32 && left + m < width; m++)
                pixels[row][left + m]=((x & (1 << m)) != 0)?1:0;
        }
        return pixels;
    }
    public int[] nextGlyph(int width, int height, int[] bitmask, int remainingMillis) {
        if (firstCode==0) {
            firstCode++;
            glyphStart=remainingMillis;
        }
        else letters.get(fontSize)[letterId-32]=processedBitmask(width, height, bitmask);
        if (letterId < 126)
            letterId++;
        else if (fontSize<72) { //for now stick to a single font
            letterId=32;
            fontSize++;
            letters.put(fontSize, new int[127-32][][]);
        }
        else {
            glyphEnd=remainingMillis;
            System.out.println("GLYPH RETRIEVAL: "+(glyphStart-glyphEnd)+" ms");
            return new int[] {};
        }
        return new int[] {letterId, fontId, fontSize};
    }
    private double score(int[][] newspace, int SATURATION, int[][] newLayer, int[][] space, int precision) {
        double score=0;
        for (int x=0; x<space.length; x+=precision) {
            for (int y=0; y<space[x].length; y+=precision) {
                int xb=Math.min(x+precision, space.length);
                int yb=Math.min(y+precision, space[x].length);
                int newSum=0, origSum=0;
                for (int i=x; i<xb; i++) {
                    for (int j=y; j<yb; j++) {
                        newSum+=SATURATION*newspace[i][j]+newLayer[i][j];
                        origSum+=space[i][j];
                    }
                }
                double diff=Math.abs((double)(newSum-origSum))/((xb-x)*(yb-y)*255);

                score+=(1-diff)*(1-diff);
            }
        }
        return score;
    }
    private int[][] textToSpace(ArrayList<ArrayList<Integer>> text, int width, int height, int[][][] specLetters) {
        int[][] space=new int[width][height];
        int x=0;
        for (int i=0; i<text.size(); i++) {
            int maxW=0;
            int y=0;
            for (int j=0; j<text.get(i).size(); j++) {
                int w=specLetters[text.get(i).get(j)].length;
                int h=specLetters[text.get(i).get(j)][0].length;
                for (int xi=0; xi<=w; xi++) {
                    for (int yi=0; yi<=h; yi++) {
                        try {
                            space[xi+x][yi+y]=specLetters[text.get(i).get(j)][xi][yi];
                        }
                        catch (Exception e) {
                            break; //out of bounds of space
                        }
                    }
                }
                y+=h;
                if (w>maxW) maxW=w;
            }
            x+=maxW;
        }
        return space;
    }
    private void textSpaceReplaceLetter(int[][] space, int tx, int ty, ArrayList<ArrayList<Integer>> text, int[][][] specLetters) {
        //modify space at the tx -th line
        int x=0;
        for (int i=0; i<tx; i++) x+=specLetters[text.get(i).get(0)].length;
        int w=specLetters[text.get(tx).get(0)].length; //width of letters (but is called height)
        int y=0;
        for (int j=0; j<ty; j++) y+=specLetters[text.get(tx).get(ty)][0].length;
        for (int j=ty; j<text.get(tx).size(); j++) {
            int h=specLetters[text.get(tx).get(j)][0].length;
            for (int xi=0; xi<=w; xi++) {
                for (int yi=0; yi<=h; yi++) {
                    try {
                        space[xi+x][yi+y]=specLetters[text.get(tx).get(j)][xi][yi];
                    }
                    catch (Exception e) {
                        break; //out of bounds of space
                    }
                }
            }
            y+=h;
        }
        while (y<space[0].length) { //erase everything to the right
            for (int xi=0; xi<=w; xi++) {
                try {
                    space[xi+x][y]=0;
                }
                catch (Exception e) {
                    break; //out of bounds of space
                }
            }
            y++;
        }
    }
    private static double acceptanceProb(double s0, double s1, double temp) {
        return (s1>s0)?1:Math.exp((s1-s0)/temp);
    }
    /**
     * returns ArrayList of text layouts for a single color space
     */
    private ArrayList<ArrayList<ArrayList<Integer>>> bestLayouts(int[][] space) {
        return null;
    }
    //iterated nRenders times
    public int[] render(int iteration, int remainingMillis) {
        if (firstCode==1) {
            firstCode++;
            renderStart=remainingMillis;
            layerScores=new double[4];
            layerOpen=new boolean[4];
            for (int col=0; col<4; col++) {
                layerScores[col]=score(newLayers[col], 0, newLayers[col], imgLayers[col], precision);
                layerOpen[col]=true;
            }
        }
        ArrayList<ArrayList<Integer>> textLayout;
        int[][] textSpace;
        Random r$=new Random();
        int saturation;
        int font=0;
        int COLORID;
        while (true) {
            COLORID=-1;
            double colscr=Double.POSITIVE_INFINITY;
            for (int col=1; col<4; col++) {
                if (layerOpen[col] && layerScores[col]<colscr) {
                    COLORID=col;
                    colscr=layerScores[col];
                }
            }
            if (COLORID==-1) return new int[] {0,8,0,0};
            fontSize=r$.nextInt(73-8)+8;
            int ltrHeight=letters.get(fontSize)[0].length;
            textLayout=new ArrayList<>();
            for (int x=0; x*ltrHeight<WIDTH; x++) {
                textLayout.add(new ArrayList<>());
                for (int y=0; y*ltrHeight<1.5*HEIGHT; y++) { //chars are usually less wide than tall
                    textLayout.get(x).add(r$.nextInt(127-32));
                }
            }
            saturation=r$.nextInt(255);
            textSpace=textToSpace(textLayout, WIDTH, HEIGHT, letters.get(fontSize));
            double score=score(textSpace, saturation, newLayers[COLORID], imgLayers[COLORID], precision);
            System.out.print("(orig "+layerScores[COLORID]+") #"+COLORID+": score="+score);
            double temp=1;
            double[] mutationWeights=new double[2];
            for (int i=0; i<mutationWeights.length; i++)
                mutationWeights[i]=(i+1.0)/(mutationWeights.length+1.0);
            /*SA
            change font size
            change saturation
            change one char
            */
            for (int REP=0; REP<200; REP++) {
                double decision=r$.nextDouble();
                if (decision<mutationWeights[0]) {
                    int oldSize=fontSize;
                    int spread=(int)(1+10*temp);
                    int min=Math.max(8, fontSize-spread);
                    int max=Math.min(min+2*spread, 72);
                    fontSize=r$.nextInt(max-min+1)+min;
                    int[][] newTextSpace=textToSpace(textLayout, WIDTH, HEIGHT, letters.get(fontSize));
                    double newscr=score(newTextSpace, saturation, newLayers[COLORID], imgLayers[COLORID], precision);
                    if (r$.nextDouble()<acceptanceProb(score, newscr, temp)) {
                        score=newscr;
                        for (int i=0; i<textSpace.length; i++)
                            textSpace[i]=newTextSpace[i].clone();
                    }
                    else fontSize=oldSize;
                }
                else if (decision<mutationWeights[1]) {
                    int oldSat=saturation;
                    int spread=(int)(5+50*temp);
                    int min=Math.max(0, saturation-spread);
                    int max=Math.min(min+2*spread, 255);
                    saturation=r$.nextInt(max-min+1)+min;
                    double newscr=score(textSpace, saturation, newLayers[COLORID], imgLayers[COLORID], precision);
                    if (r$.nextDouble()<acceptanceProb(score, newscr, temp)) score=newscr;
                    else saturation=oldSat;
                }
                else { //change one char
                    int x=r$.nextInt(textLayout.size());
                    int y=r$.nextInt(textLayout.get(x).size());
                    int charNum=r$.nextInt(127-32);
                    //give preference to space char?
                    int oldCharNum=textLayout.get(x).get(y);
                    textLayout.get(x).set(y, charNum);
                    textSpaceReplaceLetter(textSpace, x, y, textLayout, letters.get(fontSize));
                    double newscr=score(textSpace, saturation, newLayers[COLORID], imgLayers[COLORID], precision);
                    if (r$.nextDouble()<acceptanceProb(score, newscr, temp))
                        score=newscr;
                    else {
                        textLayout.get(x).set(y, oldCharNum);
                        textSpaceReplaceLetter(textSpace, x, y, textLayout, letters.get(fontSize));
                    }
                }
                temp*=0.98;
                for (int i=0; i<mutationWeights.length; i++)
                    mutationWeights[i]*=0.995;
            }
            if (score<=layerScores[COLORID]) {
                layerOpen[COLORID]=false;
                System.out.println();
            }
            else {
                layerScores[COLORID]=score;
                System.out.println("->"+score);
                break;
            }
        }
        //update newLayers
        for (int x=0; x<WIDTH; x++)
            for (int y=0; y<HEIGHT; y++)
                newLayers[COLORID][x][y]+=textSpace[x][y]*saturation;
        int totLtrCnt=0;
        for (ArrayList<Integer> row:textLayout) totLtrCnt+=row.size();
        int[] out=new int[4+textLayout.size()+totLtrCnt];
        out[0]=font; //font
        out[1]=fontSize;
        out[2]=COLORID; //color id
        out[3]=saturation;
        int id=4;
        for (int x=0; x<textLayout.size(); x++) {
            for (int y=0; y<textLayout.get(x).size(); y++) {
                out[id]=textLayout.get(x).get(y)+32;
                id++;
            }
            out[id]=10;
            id++; //carriage return
        }
        if (iteration==nRenders-1) {
            renderEnd=remainingMillis;
            System.out.println("RENDERING: "+(renderStart-renderEnd)+" ms");
        }
        return out;
    }
    /*public static void main(String[] args) {
        System.out.println("compiles");
    }*/
    public static void main(String[] args) throws IOException {
        TypesetterArt solution = new TypesetterArt();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            int nRenders = Integer.parseInt(in.readLine());
            int nFonts = Integer.parseInt(in.readLine());
            int precision = Integer.parseInt(in.readLine());
            int width = Integer.parseInt(in.readLine());
            int height = Integer.parseInt(in.readLine());
            {
                int n = Integer.parseInt(in.readLine());
                int[] image = new int[n];

                for (int i = 0; i < n; i++) {
                    image[i] = Integer.parseInt(in.readLine());
                }

                System.err.printf("Calling init\n");

                int d = solution.init(nRenders, nFonts, precision, width, height, image);

                System.err.printf("Return from init: %d\n", d);

                System.out.printf("%d\n", d);
                System.out.flush();
            }

            boolean callNext = true;
            while (callNext) {
                int glyphWidth = Integer.parseInt(in.readLine());
                int glyphHeight = Integer.parseInt(in.readLine());
                int n = Integer.parseInt(in.readLine());
                int[] bitmask = new int[n];
                for (int i = 0; i < n; i++) {
                    bitmask[i] = Integer.parseInt(in.readLine());
                }
                int remainingMillis = Integer.parseInt(in.readLine());

                System.err.printf("Calling next glyph\n");

                int[] ret = solution.nextGlyph(glyphWidth, glyphHeight, bitmask, remainingMillis);

                System.err.printf("Return from next glyph size: %d\n", ret.length);

                if (ret.length == 0) {
                    callNext = false;
                    System.out.printf("\n");
                }
                else {
                    System.out.printf("%d %d %d\n", ret[0], ret[1], ret[2]);
                }
                System.out.flush();
            }

            for (int i = 0; i < nRenders; i++) {
                int remainingMillis = Integer.parseInt(in.readLine());

                System.err.printf("Calling render\n");

                int[] ret = solution.render(i, remainingMillis);

                System.err.printf("Return from render size: %d\n", ret.length);

                System.out.printf("%d\n", ret.length);
                for (int x : ret) {
                    System.out.printf("%d\n", x);
                }
                System.out.flush();
            }
        }
    }
}