import java.io.*;
import java.math.BigInteger;
import java.util.*;
public class NumberCreator {
    private static final char PLUS='+', MINUS='-', TIMES='*', OVER='/';
    private int NUM0, NUM1;
    class Sequence {
        private ArrayList<String> lines; //list of operations done, checking for duplicate number making
        private Map<BigInteger, Integer> idxs;
        private ArrayList<BigInteger> nums; //numbers generated from all operations
        private Sequence() {
            lines = new ArrayList<>();
            nums = new ArrayList<>();
            idxs = new HashMap<>();
            nums.add(new BigInteger(NUM0 + ""));
            nums.add(new BigInteger(NUM1 + ""));
            for (int i = 0; i < nums.size(); i++)
                idxs.put(nums.get(i), i);
        }
        private void add(BigInteger n0, BigInteger n1, char op) {
            BigInteger n2 = op == PLUS ? n0.add(n1) :
                    op == MINUS ? n0.subtract(n1) :
                            op == TIMES ? n0.multiply(n1) :
                                    op == OVER ? n0.divide(n1) : null;
            String line = "" + idxs.get(n0) + " " + op + " " + idxs.get(n1);
            nums.add(n2);
            if (!idxs.containsKey(n2)) {
                lines.add(line);
                idxs.put(n2, idxs.keySet().size());
            }
        }
        private void makeNum(String target, BigInteger baseNum) {
            if (new BigInteger(target).compareTo(BigInteger.ZERO)==0)
                return;
            ArrayList<BigInteger> digits = balancedDigits(target, baseNum);
            add(nums.get(0), nums.get(0), '/');
            BigInteger half=baseNum.divide(new BigInteger("2"));
            for (int i = 2; new BigInteger(i+"").compareTo(half)<=0; i++)
                add(new BigInteger((i - 1) + ""), BigInteger.ONE, '+');
            add(half,half,'+');
            if (baseNum.mod(new BigInteger("2")).compareTo(BigInteger.ONE)==0)
                add(baseNum.subtract(BigInteger.ONE),BigInteger.ONE, '+');
            BigInteger num=digits.get(digits.size()-1);
            for (int i = digits.size() - 2; i > -1; i--) {
                add(num, baseNum, '*');
                num=num.multiply(baseNum);
                if (digits.get(i).compareTo(BigInteger.ZERO)<0)
                    add(num, BigInteger.ZERO.subtract(digits.get(i)), '-');
                else
                    add(num, digits.get(i), '+');
                num = num.add(digits.get(i));
            }
        }
        private void makeNums(List<BigInteger> nums, BigInteger bnum) {
            if (nums.size()==0)
                return;
            List<BigInteger> sorted=new ArrayList<>(nums);
            Collections.sort(sorted);
            makeNum(sorted.get(0)+"",bnum);
            for (int i=1; i<sorted.size(); i++) {
                BigInteger high=sorted.get(i), low=sorted.get(i-1);
                makeNum(high.subtract(low) + "", bnum);
                add(low,high.subtract(low),'+');
            }
        }
    }
    private static ArrayList<BigInteger> digits(String target, BigInteger baseNum) {
        ArrayList<BigInteger> digits = new ArrayList<>();
        BigInteger help = new BigInteger(target);
        while (help.compareTo(BigInteger.ZERO) > 0) {
            digits.add(help.mod(baseNum));
            help = help.divide(baseNum);
        }
        return digits;
    }
    private static ArrayList<BigInteger> balancedDigits(String target, BigInteger baseNum) {
        ArrayList<BigInteger> digits = digits(target, baseNum);
        BigInteger half=baseNum.divide(new BigInteger("2"));
        for (int i=0; i<digits.size(); i++) {
            if (digits.get(i).compareTo(half)>0) {
                digits.set(i,digits.get(i).subtract(baseNum));
                if (i+1==digits.size())
                    digits.add(BigInteger.ZERO);
                digits.set(i+1,digits.get(i+1).add(BigInteger.ONE));
                for (int j=i+1; j<digits.size() && digits.get(j).compareTo(baseNum)==0; j++) {
                    digits.set(j,digits.get(j).subtract(baseNum));
                    if (j+1==digits.size())
                        digits.add(BigInteger.ZERO);
                    digits.set(j+1,digits.get(j+1).add(BigInteger.ONE));
                }
            }
        }
        return digits;
    }
    private static String inBase(String n, int base) {
        ArrayList<BigInteger> digits = balancedDigits(n, new BigInteger(base+""));
        StringBuilder str=new StringBuilder();
        for (int i=digits.size()-1; i>-1; i--)
            str.append(Integer.parseInt(""+digits.get(i))+(i==0?"":","));
        return str.toString();
    }
    public String[] findSolution(int NUM0, int NUM1, String target) {
        long END=System.currentTimeMillis()+9700;
        this.NUM0=NUM0;
        this.NUM1=NUM1;
        ArrayList<String> best=new ArrayList<>();
        int scr=Integer.MAX_VALUE;
        int bbase=-1, bb=-1, btype=-1;
        //CHANGE MIDDLE PART FO LINE BELOW TO true FOR _search mode
        PrintStream PRINT=System.err;
        for (int base=10; true; base++) {
            if (System.currentTimeMillis()>END) {
                PRINT.println("base<"+base);
                break;
            }
            BigInteger baseNum = new BigInteger(base + "");
            ArrayList<BigInteger> digits = balancedDigits(target, baseNum);
            Set<BigInteger> absdigits=new TreeSet<>();
            for (BigInteger d:digits)
                absdigits.add(d.abs());
            //absdigits.add(baseNum);
            List<BigInteger> toMake=new ArrayList<>(absdigits);
            Sequence seq=null;
            int b1=-1, bt1=-1;
            /*for (int b=2; b<=20; b++) {
                Sequence tseq = new Sequence();
                for (BigInteger n : absdigits)
                    tseq.makeNum(n + "",new BigInteger(b+""));
                if (seq==null || tseq.lines.size()<seq.lines.size()) {
                    seq = tseq;
                    b1=b;
                }
            }*/
            for (int b=2; b<=20; b++) {
                Sequence tseq = new Sequence();
                BigInteger bnum=new BigInteger(b+"");
                tseq.makeNums(toMake,bnum);
                if (seq==null || tseq.lines.size()<seq.lines.size()) {
                    seq = tseq;
                    b1=b;
                    bt1=1;
                }
            }
            for (int b=2; b<=20; b++) {
                Sequence tseq = new Sequence();
                BigInteger bnum=new BigInteger(b+"");
                tseq.makeNum(toMake.get(0)+"",bnum);
                ArrayList<BigInteger> diffs=new ArrayList<>();
                for (int i=1; i<toMake.size(); i++)
                    diffs.add(toMake.get(i).subtract(toMake.get(i-1)));
                tseq.makeNums(diffs,bnum);
                for (int i=1; i<toMake.size(); i++) {
                    BigInteger high=toMake.get(i), low=toMake.get(i-1);
                    tseq.add(low,high.subtract(low),'+');
                }
                if (seq==null || tseq.lines.size()<seq.lines.size()) {
                    seq = tseq;
                    b1=b;
                    bt1=2;
                }
            }
            BigInteger maxDigit=new ArrayList<>(absdigits).get(absdigits.size()-1);
            BigInteger quot=baseNum.divide(maxDigit), rem=baseNum.mod(maxDigit);
            seq.makeNum(quot+"",new BigInteger(b1+""));
            seq.makeNum(rem+"",new BigInteger(b1+""));
            seq.add(maxDigit,quot,'*');
            seq.add(maxDigit.multiply(quot),rem,'+');
            BigInteger num=digits.get(digits.size()-1);
            for (int i = digits.size() - 2; i > -1; i--) {
                seq.add(num, baseNum, '*');
                num=num.multiply(baseNum);
                if (digits.get(i).compareTo(BigInteger.ZERO)<0)
                    seq.add(num, BigInteger.ZERO.subtract(digits.get(i)), '-');
                else
                    seq.add(num, digits.get(i), '+');
                num = num.add(digits.get(i));
            }
            if (seq.lines.size()<scr) {
                scr=seq.lines.size();
                best = new ArrayList<>(seq.lines);
                bbase=base;
                bb=b1;
                btype=bt1;
            }
        }
        PRINT.println("bbase="+bbase+"\nbb="+bb+"\nbtype="+btype+"\nbscr="+scr);
        ArrayList<BigInteger> digits = balancedDigits(target, new BigInteger(bbase+""));
        for (int i=0; i<digits.size(); i++)
            digits.set(i,digits.get(i).abs());
        digits=new ArrayList<>(new HashSet<>(digits));
        Collections.sort(digits);
        for (BigInteger d:digits)
            PRINT.print(d+" ");
        PRINT.println();
        ArrayList<BigInteger> diffs=new ArrayList<>();
        for (int i=1; i<digits.size(); i++)
            diffs.add(digits.get(i).subtract(digits.get(i-1)));
        diffs=new ArrayList<>(new HashSet<>(diffs));
        Collections.sort(diffs);
        for (BigInteger d:diffs)
            PRINT.print(d+" ");
        PRINT.println();
        String[] out=new String[best.size()];
        for (int i=0; i<out.length; i++)
            out[i]= best.get(i);
        return out;
    }
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            int Num0 = Integer.parseInt(br.readLine());
            int Num1 = Integer.parseInt(br.readLine());
            String T = br.readLine();

            NumberCreator nc = new NumberCreator();
            String[] ret = nc.findSolution(Num0, Num1, T);

            System.out.println(ret.length);
            for (int i = 0; i < ret.length; i++) System.out.println(ret[i]);
        }
        catch (Exception e) {}
    }
}