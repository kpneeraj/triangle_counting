package utils;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Created by Neeraj on 8/5/2017.
 */
public class CheckGraph {

    public void convertGraph(String filepath, int totalVertices){
        BufferedReader br = null;
        FileWriter fw=null; BufferedWriter bw=null;
        try {
            br = new BufferedReader(new FileReader(filepath));
            String sCurrentLine,edge;
            StringTokenizer st;
            int a=0,b=0,count=0;
            while ((sCurrentLine = br.readLine()) != null) {
                edge = sCurrentLine;
                if(sCurrentLine.charAt(0)=='#') continue; // these are comment lines
                st = new StringTokenizer(edge);
                while (st.hasMoreTokens()) {
                    a = Integer.parseInt(st.nextToken());
                    b = Integer.parseInt(st.nextToken());
                }
                if(a>totalVertices || b>totalVertices){
                    System.out.println("Error in vertex numbering of graph: " + filepath);
                }

            }


            bw.close();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String args[]){
        CheckGraph dirtoUnDireced = new CheckGraph();
        dirtoUnDireced.convertGraph("graphs/com-livejournal.ungraph.txt", 3997962);
    }

}
