package utils;

/**
 * Created by Neeraj on 8/14/2017.
 */
import java.io.*;
import java.util.*;

public class Simplify {

    public static void main(String[] args) throws FileNotFoundException {

        HashMap<Integer, Integer> h = new HashMap();
        String file = "graphs\\com-live-journal_simplified.txt";
        File f  = new File(file);
        Scanner fileIn = new Scanner(f);
        int count = -1;
        int max = -1;

        while(fileIn.hasNext()) {
            int u = fileIn.nextInt();
            if (u > max)
                max = u;
        }

        System.out.println(max);

//        while(fileIn.hasNext()) {
//            int x = fileIn.nextInt();
//            if (!h.containsKey(x)) {
//                count++;
//                h.put(x, count);
//            }
//        }//end while
//
//        System.out.println("Creating");
//        PrintWriter pw  = new PrintWriter(file+"_simplified.txt");
//        fileIn = new Scanner(f);
//        int c = 0;
//        while(fileIn.hasNext()) {
//            int u = fileIn.nextInt();
//            int v = fileIn.nextInt();
//            int x = h.get(u);
//            int y = h.get(v);
//            pw.println(x + " " + y);
//            c++;
//            if (c%100000==0)
//                System.out.println(c);
//        }
//        pw.close();
//

    }

}
