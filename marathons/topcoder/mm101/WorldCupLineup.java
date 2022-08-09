import java.util.Random;
public class WorldCupLineup {
    public static int[][] playerGroupStats(int[][] lineUp, int[] playing, int[][] playerStats, int[][] groupIds, int[][] groupStats) {
        boolean[] groupPresent = new boolean[groupIds.length];
        for (int i=0; i<groupPresent.length; i++) {
            groupPresent[i]=true;
            for (int j=0; j<groupIds[i].length && groupPresent[i]; j++) {
                //search groupIds[i][j] in _[_players[k]]
                int search=0;
                while (search<lineUp.length) {
                    if (playing[search]<2 && groupIds[i][j]==lineUp[search][playing[search]]) break;
                    else search++;
                }
                if (search>=lineUp.length) {
                    groupPresent[i]=false;
                }
            }
        }
        //CANNOT DO STRAIGHT EQUALS
        int[][] out=new int[playerStats.length][3];
        for (int i=0; i<out.length; i++) {
            for (int j=0; j<3; j++) {
                out[i][j]+=playerStats[i][j];
            }
        }
        for (int i=0; i<groupPresent.length; i++) {
            if (groupPresent[i]) {
                for (int j=0; j<groupIds[i].length; j++) {
                    //adjust player groupIds[i][j]
                    for (int stat=0; stat<3; stat++) out[groupIds[i][j]][stat]+=groupStats[i][stat];
                }
            }
        }
        return out;
    }
    //only up to 5 rounds
    public static double[][] trackTimeRandom(int[][] lineUp, int[][] playerStats, int[][] groupIds, int[][] groupStats, int rounds) {

        double[][] times=new double[lineUp.length][3];
        for (int i=0; i<times.length; i++) for (int j=0; j<3; j++) times[i][j]=0;
        int[][] cards=new int[lineUp.length][3];
        for (int i=0; i<cards.length; i++) for (int j=0; j<3; j++) cards[i][j]=0;
        int[] playing=new int[lineUp.length]; for (int i=0; i<playing.length; i++) playing[i]=0;
        int limit;
        for (int STAGE=0; STAGE<rounds; STAGE++) {
            int[][] playerGroupStats=playerGroupStats(lineUp, playing, playerStats, groupIds, groupStats);
            //update who is playing right now
            for (int i=0; i<playing.length; i++) {
                //players first play, and see if they get a card
                //use random double 0-1
                if (playing[i]<3 && lineUp[i][playing[i]]>-1) {
                    times[i][playing[i]]++;
                    double AGG=playerGroupStats[ lineUp[i][playing[i]] ][2];
                    AGG/=100;
                    //System.out.println(playerGroupStats[ lineUp[i][playing[i]] ][2]*100);
                    if (AGG>0 && new Random().nextDouble()<AGG) {
                        cards[i][playing[i]]++;
                        if (cards[i][playing[i]]==2) playing[i]++;
                    }
                }
            }
        }
        return times;
    }
    public static double[][] expectedTimeRandom(int[][] lineUp, int[][] playerStats, int[][] groupIds, int[][] groupStats, int rounds) {

        double[][] avgtimes=new double[lineUp.length][3];
        for (int i=0; i<avgtimes.length; i++) for (int j=0; j<3; j++) avgtimes[i][j]=0;
        int TRIALS=100;
        for (int n=0; n<TRIALS; n++) {
            double[][] trial=trackTimeRandom(lineUp, playerStats, groupIds, groupStats, rounds);
            for (int i=0; i<avgtimes.length; i++) for (int j=0; j<3; j++) avgtimes[i][j]+=trial[i][j];
        }
        for (int i=0; i<avgtimes.length; i++) for (int j=0; j<3; j++) avgtimes[i][j]/=TRIALS;
        return avgtimes;
    }
    //overall MIN(ATK,DEF) in current stage
    public static double singleRoundOverall(int[] positions, int[][] lineUp, int[] playing, int[][] playerStats) {
        int overallDEF=0;
        int overallATK=0;
        for (int j = 0; j < lineUp.length; j++) {
            if (playing[j]<3) {
                int player = lineUp[j][playing[j]];
                if (player > -1) {
                    overallATK+=(2-positions[j])*playerStats[player][0];
                    overallDEF+=positions[j]*playerStats[player][1];
                }
            }
        }
        if (overallATK<overallDEF) return overallATK; else return overallDEF;
    }
    public static double doublearraysum(double[] list, int a, int b) {
        double sum=0;
        for (int i=a; i<=b && i<list.length; i++) sum+=list[i];
        return sum;
    }
    //overall ATK and DEF across ALL STAGES
    public static double overall(int[] positions, int[][] lineUp, int[][] playerStats, int[][]groupIds, int[][]groupStats, int expectedTimeRounds) {
        int[] playing=new int[lineUp.length]; for (int i=0; i<playing.length; i++) playing[i]=0;
        double[][] times=expectedTimeRandom(lineUp, playerStats, groupIds, groupStats, expectedTimeRounds);
        boolean stop=false;
        double overall=0;

        for (int STAGE=0; !stop; STAGE++) {

            //update who is playing right now
            stop=true;
            for (int i=0; i<playing.length; i++) {
                if (STAGE>=doublearraysum(times[i], 0, playing[i])) {
                    playing[i]++;
                }
                if (playing[i]<3 && lineUp[i][playing[i]]>-1) stop=false;
            }
            overall+=singleRoundOverall(positions, lineUp, playing, playerGroupStats(lineUp, playing, playerStats, groupIds, groupStats) );
        }
        return overall;
    }
    //HIGHEST FIRST
    public static int[] orderBy(double[] list) {
        double[] amount = new double[list.length];
        for (int i = 0; i < list.length; i++) amount[i] = list[i];
        int[] playerOrder = new int[amount.length];
        for (int i = 0; i < playerOrder.length; i++) playerOrder[i] = i;
        //sort ascending order by ATK (insertion)
        for (int a = 1; a < amount.length; a++) {
            for (int b = a; b > 0 && amount[b - 1] < amount[b]; b--) {
                double swaptmp0 = amount[b];
                amount[b] = amount[b - 1];
                amount[b - 1] = swaptmp0;
                int swaptmp = playerOrder[b];
                playerOrder[b] = playerOrder[b - 1];
                playerOrder[b - 1] = swaptmp;
            }
        }
        return playerOrder;
    }
    //having F, D, and M, and wanting to fill idx spot of F if type==0, M if ==1, D if ==2
    public static int selectBest(int pos, int tier, int[] positions, int[][] lineUp, int[][] playerStats, int[][] groupIds, int[][] groupStats, boolean[] seen, int expectedTimeRounds) {
        //0 is F, 1 is M, 2 is D
        int maxJ=-1; //set to a sentinel value because we don't know which F s have not been seen yet
        double value=-1;
        for (int j=0; j<playerStats.length; j++) { //search all unassigned players for potential
            //make sure this is not already assigned a position and backup
            if (!seen[j]) {
                //temporary
                lineUp[pos][tier]=j;
                double testValue=overall(positions, lineUp, playerStats, groupIds, groupStats, expectedTimeRounds);
                if (testValue > value || maxJ==-1) { //for some reason this is necessary
                    maxJ = j;
                    value = testValue;
                }
                //remove temporary
                lineUp[pos][tier]=-1;
            }
        }
        return maxJ;
    }
    public static String[] selectPositions(String[] players, String[] groups) {
        int expectedTimeRounds=15;
        //CHANGE TO USABLE ARRAY FORMAT
        int[][] playerStats=new int[players.length][3];
        for (int i=0; i<players.length; i++) {
            String[] tmp=players[i].split(",");
            for (int j=0; j<tmp.length; j++) {
                playerStats[i][j]=Integer.parseInt(tmp[j]);
            }
        }
        int[][] groupIds=new int[groups.length][];
        for (int i=0; i< groups.length; i++) {
            String[] tmp=groups[i].split(" ")[0].split(",");
            int[] tmp2=new int[tmp.length];
            for (int j=0; j<tmp.length; j++) {
                tmp2[j]=Integer.parseInt(tmp[j]);
            }
            groupIds[i]=tmp2;
        }
        int[][] groupStats=new int[groups.length][3];
        for (int i=0; i< groups.length; i++) {
            String[] tmp=groups[i].split(" ")[1].split(",");
            for (int j=0; j<tmp.length; j++) {
                groupStats[i][j]=Integer.parseInt(tmp[j]);
            }
        }
        //ideal F player is one with high ATK, low AGG, and in many groups where group A high G low
        //have ppl with highest ATK*expected_time be forward
        double expectedTime;
        double bound;
        double agg;
        double[] playerATK=new double[players.length];
        double[] playerDEF=new double[players.length];
        for (int i=0; i<players.length; i++) {
            agg=playerStats[i][2]/100;
            bound=2/expectedTimeRounds;
            if (agg<=bound) expectedTime=expectedTimeRounds;
            else if (agg>=1) expectedTime=2;
            else expectedTime=2/agg;
            playerATK[i]=playerStats[i][0]*expectedTime;
            playerDEF[i]=playerStats[i][1]*expectedTime;

            //groups stuff
            for (int j=0; j<groupIds.length; j++) {
                int search=0;
                while (search<groupIds[j].length) if (i==groupIds[j][search]) break; else search++;
                if (search<groupIds[j].length) {
                    //player i is in group j
                    //assume groups are disjoint
                    agg=playerStats[ groupIds[j][0] ][2];
                    for (int k=0; k<groupIds[j].length; k++) {
                        if (playerStats[ groupIds[j][k] ][2]>agg) agg=playerStats[ groupIds[j][k] ][2];
                    }
                    agg+=groupStats[j][2]; //adjust for group j
                    bound=2/expectedTimeRounds;
                    if (agg<=bound) expectedTime=expectedTimeRounds;
                    else if (agg>=1) expectedTime=2;
                    else expectedTime=2/agg;
                    playerATK[i]+=groupStats[j][0]*expectedTime;
                    playerDEF[i]+=groupStats[j][1]*expectedTime;
                }
            }
        }
        //find ppl with highest DEF*expected_time
        int FAMOUNT;
        int MAMOUNT;
        int DAMOUNT;
        int[][] postrack=new int[11][players.length/3];
        int[][][] lineUptrack=new int[11][players.length/3][3];
        boolean[][] seentrack=new boolean[11][];
        int FAMOUNTmax=-1;
        double currentOverall=0;
        //temp vars for speed
        int[] orderByATK = orderBy(playerATK);
        int[] orderByDEF = orderBy(playerDEF);
        //test all x-(10-2x)-x formations based on first level F/D players
        for (int FAMOUNTtmp=5; FAMOUNTtmp<6; FAMOUNTtmp++) {
            //--------POSITION MODEL--------
            FAMOUNT = FAMOUNTtmp;
            MAMOUNT = 10-2*FAMOUNTtmp;
            DAMOUNT = FAMOUNTtmp;
            //0=F, 1=M, 2=D
            int[] postmp = new int[players.length/3];
            for (int i = 0; i < FAMOUNT; i++) postmp[i] = 0;
            for (int i = FAMOUNT; i < FAMOUNT+MAMOUNT; i++) postmp[i] = 1;
            for (int i = FAMOUNT+MAMOUNT; i < FAMOUNT+MAMOUNT+DAMOUNT; i++) postmp[i] = 2;
            int Fp = 0;
            int Dp=0;
            int[][] lineUptmp=new int[players.length/3][3];
            for (int i=0; i<lineUptmp.length; i++) for (int j=0; j<3; j++) lineUptmp[i][j]=-1;
            //choose first STAGE F and D players
            boolean[] seentmp = new boolean[players.length]; for (int i = 0; i < seentmp.length; i++) seentmp[i] = false;
            //var above will be used later
            for (int i = 0; Fp < FAMOUNT || Dp < DAMOUNT; i++) {
                //symmetric decision
                int topAPlayer = orderByATK[i];
                int topDPlayer = orderByDEF[i];
                if (Fp < FAMOUNT && Dp < DAMOUNT && topAPlayer == topDPlayer) { //same rank: tiebreaker by overall ATK/DEF
                    lineUptmp[Fp][0] = topAPlayer;
                    double tmp = overall(postmp, lineUptmp, playerStats, groupIds, groupStats, expectedTimeRounds);
                    lineUptmp[Fp][0] = -1;
                    lineUptmp[FAMOUNT+MAMOUNT+Dp][0] = topAPlayer;
                    if (tmp > overall(postmp, lineUptmp, playerStats, groupIds, groupStats, expectedTimeRounds)) {
                        lineUptmp[FAMOUNT+MAMOUNT+Dp][0] = -1;
                        lineUptmp[Fp][0] = topAPlayer;
                        Fp++;
                        seentmp[topAPlayer] = true;
                    } else {
                        Dp++;
                        seentmp[topDPlayer] = true;
                    }
                } else {
                    if (Fp < FAMOUNT && !seentmp[topAPlayer]) {
                        lineUptmp[Fp][0] = topAPlayer;
                        Fp++;
                        seentmp[topAPlayer] = true;
                    }
                    if (Dp < DAMOUNT && !seentmp[topDPlayer]) {
                        lineUptmp[FAMOUNT+MAMOUNT+Dp][0] = topDPlayer;
                        Dp++;
                        seentmp[topDPlayer] = true;
                    }
                }
            }
            //now M
            for (int i=0; i<postmp.length; i++) {
                if (lineUptmp[i][0]==-1) {
                    int maxJ=selectBest(i, 0, postmp, lineUptmp, playerStats, groupIds, groupStats, seentmp, expectedTimeRounds);
                    lineUptmp[i][0] = maxJ;
                    seentmp[maxJ] = true;
                }
            }
            if (FAMOUNTmax==-1 || overall(postmp, lineUptmp, playerStats, groupIds, groupStats, expectedTimeRounds)>currentOverall) {
                FAMOUNTmax=FAMOUNTtmp;
                postrack[FAMOUNTmax]=postmp;
                lineUptrack[FAMOUNTmax]=lineUptmp;
                seentrack[FAMOUNTmax]=seentmp;
                currentOverall=overall(postmp, lineUptmp, playerStats, groupIds, groupStats, expectedTimeRounds);
            }
        }
        FAMOUNT=FAMOUNTmax;
        MAMOUNT=10-2*FAMOUNT;
        DAMOUNT=FAMOUNT;
        int[] positions=postrack[FAMOUNTmax];
        int[][] lineUp=lineUptrack[FAMOUNTmax];
        boolean[] seen=seentrack[FAMOUNTmax];
        int[] playing=new int[lineUp.length]; for (int i=0; i<playing.length; i++) playing[i]=0;
        double[][] times=new double[lineUp.length][3];
        //act as if the game goes on forever in order to fill all midfielders who are expected to come in after the 5 rounds
        int assignednum=lineUp.length;
        for (int STAGE=0; assignednum<players.length; STAGE++) {
            //must update this for new players
            times=expectedTimeRandom(lineUp, playerStats, groupIds, groupStats, expectedTimeRounds);
            for (int i=0; i<playing.length; i++) {
                if (STAGE>=doublearraysum(times[i], 0, playing[i])) {
                    playing[i]++;
                }
            }
            //fill open slots
            for (int i=0; i<playing.length; i++) {
                if (playing[i]<3 && lineUp[i][playing[i]]==-1) {
                    int maxJ=selectBest(i, playing[i], positions, lineUp, playerStats, groupIds, groupStats, seen, expectedTimeRounds);
                    lineUp[i][playing[i]] = maxJ;
                    seen[maxJ] = true;
                    assignednum++;
                }
            }
        }

        //converting to output format
        String[] out=new String[lineUp.length];
        for (int i=0; i<out.length; i++) {
            if (positions[i]==0) out[i]="F ";
            else if (positions[i]==1) out[i]="M ";
            else out[i]="D ";
            out[i]+=Integer.toString(lineUp[i][0]);
            for (int j=1; j<3; j++) {
                out[i]+=","+Integer.toString(lineUp[i][j]);
            }
        }
        //TESTER
        System.out.println("Overall: "+overall(positions, lineUp, playerStats, groupIds, groupStats, 5));
        ///TESTER
        return out;
    }
    // -------8<------- end of solution submitted to the website -------8<-------
    public static void main(String[] args) {
        String[][] Players=new String[3][30];
        Players[0]=new String[]{
                "60,95,6",
                "11,76,30",
                "28,85,46",
                "11,27,44",
                "97,25,49",
                "12,58,39",
                "41,95,8",
                "89,27,1",
                "29,40,28",
                "6,1,26",
                "36,32,17",
                "86,15,19",
                "63,62,19",
                "78,32,9",
                "60,99,31",
                "23,28,11",
                "85,9,5",
                "40,95,45",
                "27,11,17",
                "68,75,38",
                "39,1,32",
                "36,0,22",
                "59,20,49",
                "98,76,46",
                "28,91,35",
                "52,68,40",
                "42,35,1",
                "52,31,17",
                "31,86,49",
                "59,34,3"
        };
        Players[1]=new String[] {
                "41,97,2",
                "49,55,32",
                "80,40,46",
                "15,86,34",
                "58,15,12",
                "48,55,46",
                "68,40,8",
                "8,45,43",
                "3,97,39",
                "89,76,32",
                "80,85,26",
                "44,27,2",
                "26,89,14",
                "68,34,31",
                "67,21,3",
                "8,75,23",
                "8,71,7",
                "89,7,35",
                "35,99,22",
                "30,6,36",
                "89,17,31",
                "70,8,11",
                "17,44,26",
                "61,26,15",
                "28,37,21",
                "83,77,39",
                "69,19,14",
                "78,18,47",
                "62,88,43",
                "77,0,4",
        };
        Players[2]=new String[] {
                "91,15,42",
                "99,44,6",
                "28,44,5",
                "39,40,37",
                "16,10,36",
                "90,9,47",
                "68,55,25",
                "87,41,6",
                "46,10,47",
                "42,62,44",
                "30,62,38,",
                "9,0,10",
                "30,79,4",
                "56,27,47",
                "9,40,24",
                "8,93,11",
                "78,99,42",
                "14,44,0",
                "79,19,18",
                "97,11,22",
                "18,69,28",
                "69,36,7",
                "8,81,43",
                "47,97,20",
                "29,0,11",
                "38,73,12",
                "94,81,40",
                "34,89,6",
                "99,42,16",
                "36,88,6",
        };
        String[][] Groups=new String[3][];
        Groups[0]=new String[] {
                "16,22 -23,12,-2",
                "18,28 -5,-12,1",
                "16,27 20,-15,12",
                "2,6,25,29 23,-3,23",
                "8,18,23,24 8,-20,-16",
                "8,11,12,24 11,-14,-11",
                "10,24 19,-25,-9",
                "0,3,13,21 7,12,-12",
                "2,17 7,13,-1",
                "2,8,21 13,10,-3",
                "15,16,26 23,-10,13",
                "6,28 14,20,9",
                "9,13,15,16 19,-4,23",
                "22,25 -22,-24,-10",
                "9,23,28 -14,-16,-9",
                "5,15 23,7,13",
                "13,17,19 -14,9,-2",
                "0,6 25,-21,16",
                "8,20,21,22,29 10,-24,24",
                "11,29 -1,8,-10",
                "0,12,18 -9,-10,-10",
                "20,22,23,24 0,10,4",
        };
        Groups[1]=new String[] {
                "0,5,8,16 -6,9,-4",
                "1,9 10,-14,-3",
                "11,13,14 -18,7,-8",
                "15,16,22,24,27 -15,-6,-3",
                "11,13,20 14,-8,-25",
                "14,24 18,4,-18",
                "1,12,19 24,13,-23",
                "1,2,3,8,28 -16,-16,17",
                "4,7 16,-11,-6",
                "5,12,23,27,28 9,-11,1",
                "5,16 21,-25,-22",
                "9,26 -25,-20,25",
                "0,6 -20,-9,11",
                "1,8,12,28 22,-10,12",
                "13,18,27 15,10,-6",
                "4,8,10,20,25 22,-2,-11",
                "13,29 -9,-20,22",
                "10,16,23 8,-12,-23",
                "14,26 1,-8,-21",
                "3,25 7,25,-19",
                "0,7,10,20,25 18,-7,16",
                "19,25 -24,-22,-10",
                "24,28 25,0,-13",
                "16,18 -12,10,24",
                "9,14 9,13,25",
                "15,26 -25,10,6",
                "0,13,25 -6,-9,22",
                "2,8 17,10,20",
                "5,11 10,5,-20",
                "13,18 -19,23,-23",
                "20,23 0,-11,5",
                "2,18,19,21 9,12,-3",
                "4,7,20 10,-2,1",
                "2,6,11 19,6,15",
                "6,17 -4,-7,7",
                "10,16,22,24,25 18,8,11",
                "11,19 13,-16,-24",
                "1,13,24 -25,-9,-1",
                "6,7,10,15 21,-19,11",
                "2,27 3,25,-23",
                "11,24,26 -20,-6,-3",
                "0,13,26 8,16,-18",
                "26,28 23,-4,-1",
                "6,21 3,6,-5",
                "8,12,14,18,24,28 8,10,18",
                "19,27,29 0,7,-2",
                "18,28 -15,5,7",
                "12,27 11,-4,18",
        };
        Groups[2]=new String[] {
                "0,3,4,16,26 0,10,-10"
                ,       "10,27 21,-9,-19"
                ,      "8,17,24,27 -10,-17,-22"
                ,     "3,21,23 -15,-10,-6"
                ,    "17,24 7,24,25"
                ,   "4,29 3,-25,-24"
                ,  "9,11,28 -23,-17,11"
                , "8,24,29 19,3,12"
                ,       "0,25 6,-23,16"
                ,      "10,16 -9,21,-11"
                ,     "1,8,17 19,3,-22"
                ,    "5,27 11,-24,-17"
                ,   "5,18,29 8,4,-18"
                ,  "0,7,26 -20,-22,1"
                , "14,17,24,26 -24,25,-2"
                ,"8,11,25 -10,17,-17"
        };
        String[] Out;
        for (int i=0; i<Players.length; i++) {
            Out = selectPositions(Players[i], Groups[i]);
            //for (int j = 0; j < Out.length; j++) System.out.println(Out[j]);
        }
    }
}