package utils;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Created by Neeraj on 12/17/2016.
 */
public class DirectedToUndirected {
    HashSet<String> edges = new HashSet<String>();
    HashMap<Integer, Integer> vertexMap = new HashMap<Integer,Integer>();

    public void convertGraph(String filepath){
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
                if(vertexMap.get(a)==null){
                    vertexMap.put(a,count);
                    count++;
                }
                if(vertexMap.get(b)==null){
                    vertexMap.put(b,count);
                    count++;
                }
                String e = Integer.toString(Math.min(a,b))+ " " + Integer.toString(Math.max(a,b));
                edges.add(e);
            }

            System.out.println("Total vertex count: " + count);
            fw = new FileWriter(filepath.split("\\.")[0] + "_undirected.txt");
            bw = new BufferedWriter(fw);

            Iterator<String> iterator = edges.iterator();
            int edgeCOunt=0;
            StringBuffer sbr = new StringBuffer();
            while(iterator.hasNext()){
                edge = iterator.next();
                st = new StringTokenizer(edge);
                while (st.hasMoreTokens()) {
                    a = Integer.parseInt(st.nextToken());
                    b = Integer.parseInt(st.nextToken());
                }
                edgeCOunt++;
                sbr.setLength(0);
                bw.write(sbr.append(Integer.toString(vertexMap.get(Math.min(a,b)))+ " " + Integer.toString(vertexMap.get(Math.max(a,b)))).toString());
                bw.write("\n");
            }
            System.out.println("Total edge count: " + edgeCOunt);

            bw.close();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String args[]){
        DirectedToUndirected dirtoUnDireced = new DirectedToUndirected();
        dirtoUnDireced.convertGraph("graphs/com-livejournal.ungraph.txt");

    }


}
