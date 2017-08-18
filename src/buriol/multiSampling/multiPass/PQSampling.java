package buriol.multiSampling.multiPass;

import java.io.*;
import java.util.*;

/**
 * Created by Neeraj on 8/5/2017.
 */
public class PQSampling {
    HashSet<Integer> vertexReservoir = new HashSet<Integer>();
    ArrayList<Edge> edgeReservoir = new ArrayList<Edge>();
    String inputFile;
    int totalVertices, triangleCount=0, totalEdges=0, blueEdges=0;
    double p,q;
    HashSet<String> triangleFormed;

    /**
     * serves as the black edge reservoir.It is named as res1 since we try to integrate vertex reservoir and black edge reservoir
     * */
    HashMap<Integer,VertexInfo> res1map = new HashMap<Integer,VertexInfo>();

    HashMap<Integer,VertexInfo> res2map= new HashMap<Integer,VertexInfo>();

    //ArrayList<String> fileBuffer = new ArrayList<String>();


    public PQSampling(double p, double q, String s, int totalVertices) {
        this.p=p;
        this.q=q;
        inputFile=s;
        this.totalVertices = totalVertices;
    }

    public void clearAll(){
        edgeReservoir.clear();
        vertexReservoir.clear();
        res1map.clear();
        res2map.clear();
        triangleFormed.clear();
        triangleCount=0; totalEdges=0;blueEdges=0;
    }

    /***
     * This method runs a for loop from 0 to n and samples with probability p.
     * */
    public void sampleVertices(){
        ArrayList<Integer> vertexReservoirList = new ArrayList<Integer>();
        for(int i=0;i<totalVertices;i++){
            if(Math.random() <= p) {
                    vertexReservoirList.add(i);
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
        if(Math.random() <= q) {
            edgeReservoir.add(edge);
        }

    }

    public double getEstimateCount(){
        int uTriangleCount = this.triangleFormed.size();
        //1 -(1-pq)^3
        return  uTriangleCount / ( 1 - Math.pow((1-p*q),3));
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
                    if(res2map.containsKey(v)) {
                        isBlueCounted = true;
                        blueEdges++;
                        HashSet<Integer> l2Neighbors = res2map.get(v).neighbors;
                        HashSet<Integer> l1Neighbors = res1map.get(u).neighbors;
                        l1Neighbors.add(v);
                        HashSet<Integer> tempSet = new HashSet<Integer>(l1Neighbors);
                        tempSet.retainAll(l2Neighbors);
                        if(tempSet.size()>0)
                            addTriangle(tempSet, u,v);
                    }
                }

                if(res1map.containsKey(v)){
                    if(res2map.containsKey(u)) {
                        if(!isBlueCounted)
                            blueEdges++;
                        HashSet<Integer> l2Neighbors = res2map.get(u).neighbors;
                        HashSet<Integer> l1Neighbors = res1map.get(v).neighbors;
                        l1Neighbors.add(u);
                        HashSet<Integer> tempSet = new HashSet<Integer>(l1Neighbors);
                        tempSet.retainAll(l2Neighbors);
                        if(tempSet.size()>0)
                            addTriangle(tempSet, u,v);
                    }
                }
            }
        }
        catch(Exception e){

        }
    }

    public void addTriangle(HashSet<Integer> vertices, int u, int v){
        Iterator<Integer> itr = vertices.iterator();
        int w;
        while(itr.hasNext()){
            w = itr.next();
            int smallest, largest, middle;
            smallest = Math.min(Math.min(u,v),w);
            largest = Math.max(Math.max(u,v),w);
            if(u!=smallest && u!=largest) middle = u;
            else if(v!=smallest && v!=largest) middle = v;
            else //if(w!=smallest && w!=largest)
                middle = w;

            triangleFormed.add(smallest+","+middle+","+largest);
            triangleCount++;
        }
    }

    class Edge{
        int u,v;
        public Edge(int i, int j){
            if(i<j){
                u=i;v=j;
            }
            else{
                v=i;u=j;
            }
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


    public static void main(String args[]){
        //constants for running the comparison
        String filename="com-orkut_simplified.txt";
        int totalVertices = 3072441;
        int totalEdges = 117185083;
        int actualTriangleCount= 627584181; //this is used only for error % calculation
        int iterations=5;

        double[] ns = { 0.01, 0.05, 0.1, 0.15, 0.2,
                0.01, 0.05, 0.1, 0.15, 0.2,
                0.01, 0.05, 0.1, 0.15, 0.2,
                0.01, 0.05, 0.1, 0.15, 0.2,
                0.01, 0.05, 0.1, 0.15, 0.2};
        double[] ms = { 0.01, 0.01,0.01,0.01,0.01,
                0.05, 0.05,0.05,0.05,0.05,
                0.1,0.1,0.1,0.1,0.1,
                0.15 ,0.15 ,0.15 ,0.15 ,0.15 ,
                0.2,0.2,0.2,0.2,0.2};
        System.out.println("Multi pass multi sampling PQ version- " + filename + "\n");
        ArrayList<String> outputTable = new ArrayList<String>();
        for(int testcase=0;testcase<ns.length;testcase++){
            HashMap<Double,String> currOutputMap = new HashMap<Double,String> ();
            System.out.println("\n\nTEst case result for n=" + ns[testcase] + " and m="+ms[testcase]);
            PQSampling r = new PQSampling(ns[testcase],ms[testcase],"graphs\\"+filename, totalVertices);
            System.out.println("Multiple sampling algorithm with fixed p and q:");
            System.out.format("\n%-10s,%-10s,%-20s,%-20s,%-20s,%-15s,%-15s,%-15s,%-15s,%-20s",

                    "p="+ns[testcase], "q="+ms[testcase], "Vertices sampled", "Red edges sampled" ,"Black edges sampled", "Total size",
                    "Exact count", "Estimate", "Error %", "Time taken");

            double estimates[] = new double[iterations];
            for(int i=0;i<iterations;i++) {
                double startTime = System.currentTimeMillis();
                r.sampleVertices();
                r.sampleEdges();
                r.getCounts();
                estimates[i] = r.getEstimateCount();
                double endTime = System.currentTimeMillis();
                String op = String.format("%-10s,%-10s,%-20s,%-20s,%-20s,%-15s,%-15s,%-15s,%-15s,%-20s",
                        r.p, r.q, r.vertexReservoir.size(), r.edgeReservoir.size(), r.blueEdges,
                        (r.edgeReservoir.size() + r.blueEdges) ,  r.triangleFormed.size(), estimates[i],
                        100*( actualTriangleCount-estimates[i])/(double)actualTriangleCount,(endTime-startTime)/1000);
                System.out.println(op);
                r.clearAll();
                currOutputMap.put(estimates[i],op);

            }


            Arrays.sort(estimates);
            System.out.println("\nMedian:" + estimates[iterations/2]);
            double sum=0;
            for(int i=0;i<iterations;i++) {
                sum+=estimates[i];
            }
            System.out.println("Average:" + sum/iterations);
            outputTable.add(currOutputMap.get(estimates[iterations/2])+",   "+sum/iterations);
            printOutputs(outputTable);
        }
    }


    public static void printOutputs(ArrayList<String> outList){
        System.out.println("################ Consolidated result till now: ###############");

        System.out.format("%-10s,%-10s,%-10s,%-15s,%-15s,%-15s,%-20s,%-30s,%-20s,%-20s,%-20s\n",
                "p","q","Vertices", "Edges","Black edges", "Total memory","Exact count", "Estimate","Error %","Time taken","Average");
        Iterator<String> itr  = outList.iterator();
        while(itr.hasNext()){
            System.out.println(itr.next());
        }
        System.out.println("################################################################");
    }


}
