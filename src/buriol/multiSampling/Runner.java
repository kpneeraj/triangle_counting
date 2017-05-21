package buriol.multiSampling;

import java.io.*;
import java.util.*;

/**
 * Created by Neeraj on 10/16/2016.
 */
public class Runner {
    HashSet<Integer> vertexReservoir = new HashSet<Integer>();
    ArrayList<Edge> edgeReservoir = new ArrayList<Edge>();
    String inputFile;
    int totalVertices, triangleCount=0, totalEdges=0, vreservoirCapcity, eReservoirCapacity,blueEdges=0;
    HashSet<String> triangleFormed;

    HashMap<Integer,VertexInfo> res1map = new HashMap<Integer,VertexInfo>();
    HashMap<Integer,VertexInfo> res2map= new HashMap<Integer,VertexInfo>();
    HashMap<Integer,VertexInfo> res3map= new HashMap<Integer,VertexInfo>();

    //ArrayList<String> fileBuffer = new ArrayList<String>();


    public Runner(int i, int i1, String s, int totalVertices) {
        vreservoirCapcity = i;
        eReservoirCapacity = i1;
        inputFile=s;
        this.totalVertices = totalVertices;
    }

    public void clearAll(){
        edgeReservoir.clear();
        vertexReservoir.clear();
        res1map.clear();
        res2map.clear();
        res3map.clear();
        triangleFormed.clear();
        triangleCount=0; totalEdges=0;blueEdges=0;
    }

    /***
     * This method runs a for loop from 0 to n and mocking the vertex stream
     * and does a reservior sampling on the intergers from 1 to n.
     * */
    public void sampleVertices(){
        ArrayList<Integer> vertexReservoirList = new ArrayList<Integer>();
        for(int i=0;i<totalVertices;i++){
            if(i<vreservoirCapcity){
                vertexReservoirList.add(i);
            }
            else{
                int random = (new Random().nextInt(i));
                //Boolean isHead = <reservoirCapacity  ;
                if(random<vreservoirCapcity){
                    int randomIndex = new Random().nextInt(vreservoirCapcity);
                    vertexReservoirList.remove(randomIndex);
                    vertexReservoirList.add(randomIndex,i);
                    // true;
                }
            }
        }
        this.vertexReservoir.addAll(vertexReservoirList);
    }

    private void sampleEdges() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(inputFile));
            String sCurrentLine;
            String[] vertices = new String[2];
            while ((sCurrentLine = br.readLine()) != null) {
                //vertices = sCurrentLine.split("\t");
                if(sCurrentLine.charAt(0)=='#') continue; // these are comment lines
                StringTokenizer st = new StringTokenizer(sCurrentLine);
                while (st.hasMoreTokens()) {
                    vertices[0] = st.nextToken();
                    vertices[1] = st.nextToken();
                }
                totalEdges++;
                sampleEdge(new Edge(Integer.parseInt(vertices[0]),Integer.parseInt(vertices[1])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sampleEdge(Edge edge) {
        if(totalEdges<=eReservoirCapacity){
            edgeReservoir.add(edge);
        }
        else{
            int random = (new Random().nextInt(totalEdges));
            //Boolean isHead = <reservoirCapacity  ;
            if(random<eReservoirCapacity){
                int randomIndex = new Random().nextInt(eReservoirCapacity);
                edgeReservoir.remove(randomIndex);
                edgeReservoir.add(randomIndex,edge);
                // true;
            }
        }
        //return false;
    }

    public static void main(String args[]){
        //constants for running the comparison
        String filename="CA-CondMat.txt";
        int totalVertices = 23133;

      //  int n=50000, m=500000;
        int iterations=3;

        int testCases = 4;
        int[] ns = {23133,
                2000,
                5000,5000,
                100000,
                100000,
                500000,
                1000000};
        int[] ms = {93497,20000,50000,100000,
                250000,
                500000,
                1000000,
                1000000,
                5000000,
                10000000 };

        for(int testcase=0;testcase<testCases;testcase++){
            System.out.println("\n\nTEst case result for n=" + ns[testcase] + " and m="+ms[testcase]);
            Runner r = new Runner(ns[testcase],ms[testcase],"graphs\\"+filename, totalVertices);
            System.out.println("Multiple sampling algorithm:");
            System.out.format("\n%-20s%-20s%-20s%-20s%-20s%-20s%-20s", "Iteration", "Vertex memory(n)", "Edge memory(m)","Black edges sampled", "Exact count", "Estimate","Time taken");

            double estimates[] = new double[iterations];
            for(int i=0;i<iterations;i++) {
                double startTime = System.currentTimeMillis();
                r.sampleVertices();
                r.sampleEdges();
                r.getCounts();
                estimates[i] = r.getEstimateCount();
                double endTime = System.currentTimeMillis();
                System.out.format("\n%-20s%-20s%-20s%-20s%-20s%-20s%-20s", i, r.vreservoirCapcity, r.eReservoirCapacity, r.blueEdges, r.triangleFormed.size(), estimates[i],(endTime-startTime)/1000);
                r.clearAll();
            }

            Arrays.sort(estimates);
            System.out.println("\nMedian:" + estimates[iterations/2]);
            double sum=0;
            for(int i=0;i<iterations;i++) {
                sum+=estimates[i];
            }
            System.out.println("\nAverage:" + sum/iterations);
        }
    }

    public double getEstimateCount(){
        int uTriangleCount = this.triangleFormed.size();
        double estimate = ((((double)totalEdges*(double)totalVertices))/((double)vreservoirCapcity*eReservoirCapacity))*(uTriangleCount/3);
        return estimate;
    }


    private void getCounts() {
        triangleFormed = new HashSet<String>();
        //Build the adjacency list
        triangleCount=0;
        boolean isBlueCounted=false;
        //add all vertices from vReservoir
        Iterator<Integer> vIterator = vertexReservoir.iterator();
        while(vIterator.hasNext()){
            int ver = vIterator.next();
            res1map.put(ver,new VertexInfo(ver));
        }
        VertexInfo vl2Info,ul2Info;
        //add all vertices from edgeReservoir
        for(int i=0;i<edgeReservoir.size();i++){
            Edge e = edgeReservoir.get(i);

            int u =e.u, v = e.v;
            vl2Info = res2map.get(v);
            ul2Info = res2map.get(u);;

            if(vl2Info==null){
                vl2Info = new VertexInfo(v);
                res2map.put(v,vl2Info);
            }
            if(ul2Info==null) {
                ul2Info = new VertexInfo(u);
                res2map.put(u,ul2Info);
            }

            vl2Info.neighbors.add(u);
            ul2Info.neighbors.add(v);

        }


        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(inputFile));
            String sCurrentLine;
            String[] vertices = new String[2];
            while ((sCurrentLine = br.readLine()) != null) {
                if(sCurrentLine.charAt(0)=='#') continue; // these are comment lines
                StringTokenizer st = new StringTokenizer(sCurrentLine);
                while (st.hasMoreTokens()) {
                    vertices[0] = st.nextToken();
                    vertices[1] = st.nextToken();
                }
                int u = Integer.parseInt(vertices[0]);
                int v = Integer.parseInt(vertices[1]);
                isBlueCounted=false;
                if(res1map.containsKey(u)){
                    int l2vertex = v;
                    int l1vertex = u;

                    if(res2map.containsKey(l2vertex)) {
                        VertexInfo vl3Info, ul3Info;
                        //add all vertices from edgeReservoir
                        vl3Info = res3map.get(v);
                        ul3Info = res3map.get(u);
                        isBlueCounted = true;
                        blueEdges++;
                        if (vl3Info == null) {
                            vl3Info = new VertexInfo(v);
                            res3map.put(v, vl3Info);
                        }
                        if (ul3Info == null) {
                            ul3Info = new VertexInfo(u);
                            res3map.put(u, ul3Info);
                        }

                        vl3Info.neighbors.add(u);
                        ul3Info.neighbors.add(v);

                        HashSet<Integer> neighbors = res2map.get(l2vertex).neighbors;
                        Iterator<Integer> neIter = neighbors.iterator();
                        while (neIter.hasNext()){
                            int currNeigh = neIter.next();

                            if( res3map.get(currNeigh)!=null){
                                HashSet<Integer> l3neighbors = res3map.get(currNeigh).neighbors;

                                if(l3neighbors.contains(l1vertex)){
                                    ArrayList<Integer> temp = new ArrayList<Integer>();
                                    temp.add(l1vertex);
                                    temp.add(l2vertex);
                                    temp.add(currNeigh);
                                    Collections.sort(temp);
                                    Iterator<Integer> itr = temp.iterator();
                                    StringBuffer str = new StringBuffer("");
                                    while(itr.hasNext()){
                                        str.append(Integer.toString(itr.next())).append(" ");
                                    }
                                    triangleFormed.add(str.toString());
                                    triangleCount++;
                                }
                            }
                        }
                    }
                }

                if(res1map.containsKey(v)){
                    int l2vertex = u;
                    int l1vertex = v;

                    if(res2map.containsKey(l2vertex)) {
                        VertexInfo vl3Info, ul3Info;
                        //add all vertices from edgeReservoir
                        vl3Info = res3map.get(v);
                        ul3Info = res3map.get(u);
                        if(!isBlueCounted ) // this check is required so that we do not add the edge count twice
                            blueEdges++;
                        if (vl3Info == null) {
                            vl3Info = new VertexInfo(v);
                            res3map.put(v, vl3Info);
                        }
                        if (ul3Info == null) {
                            ul3Info = new VertexInfo(u);
                            res3map.put(u, ul3Info);
                        }

                        vl3Info.neighbors.add(u);
                        ul3Info.neighbors.add(v);

                        HashSet<Integer> neighbors = res2map.get(l2vertex).neighbors;
                        Iterator<Integer> neIter = neighbors.iterator();
                        while (neIter.hasNext()){
                            int currNeigh = neIter.next();

                            if( res3map.get(currNeigh)!=null){
                                HashSet<Integer> l3neighbors = res3map.get(currNeigh).neighbors;

                                if(l3neighbors.contains(l1vertex)){
                                    ArrayList<Integer> temp = new ArrayList<Integer>();
                                    temp.add(l1vertex);
                                    temp.add(l2vertex);
                                    temp.add(currNeigh);
                                    Collections.sort(temp);
                                    Iterator<Integer> itr = temp.iterator();
                                    StringBuffer str = new StringBuffer("");
                                    while(itr.hasNext()){
                                        str.append(Integer.toString(itr.next())).append(" ");
                                    }
                                    triangleFormed.add(str.toString());
                                    triangleCount++;
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e){

        }

    }

    class Edge{
        int u,v;
        public Edge(int i, int j){
            u=i;v=j;
        }
    }

    class VertexInfo{
        int vertex;
        public HashSet<Integer> neighbors;
        public VertexInfo(int i){
            vertex=i;
            neighbors = new HashSet<Integer>();
        }

    }
}
