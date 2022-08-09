import java.io.*;
import java.util.*;
public class A1 {
    public static void main(String[] args) throws IOException {
        BufferedReader in=new BufferedReader(new FileReader("sort.in"));
        int N=Integer.parseInt(in.readLine());
        StringTokenizer tok=new StringTokenizer(in.readLine());
        Long[] A=new Long[N];
        for (int i=0; i<N; i++)
            A[i]=Long.parseLong(tok.nextToken());
        Arrays.sort(A);
        StringBuilder s=new StringBuilder();
        for (int i=0; i<N; i++)
            s.append(i>0?" ":"").append(A[i]);
        PrintWriter out=new PrintWriter(new FileWriter("sort.out"));
        out.println(s);
        out.close();
    }
}