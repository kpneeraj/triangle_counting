package buriol.multiSampling.singlePass;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Neeraj on 8/9/2017.
 */
public class PQSampling {

    HashSet<Integer> vertexReservoir = new HashSet<Integer>();
    ArrayList<Edge> edgeReservoir = new ArrayList<Edge>();
    String inputFile;
    double p,q;
    int blackEdgeCount, totalVertices, triangleCount=0, totalEdges=0;
    HashSet<String> triangleFormed = new HashSet<String>();
    HashMap<Integer,VertexInfo> res2map= new HashMap<Integer,VertexInfo>();
    HashMap<Integer,VertexInfo> res3map= new HashMap<Integer,VertexInfo>();

    public PQSampling(double p, double q, String s, int totalVertices) {
        this.p=p;
        this.q=q;
        this.totalVertices = totalVertices;
        this.inputFile = s;
    }

    public void clearAll(){
        edgeReservoir.clear();
        vertexReservoir.clear();
        res2map.clear();
        res3map.clear();
        triangleFormed.clear();
        triangleCount=0; totalEdges=0;
    }

    /***
     * This method runs a for loop from 0 to n and mocking the vertex stream
     * and does a reservior sampling on the integers from 1 to n.
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
                if(sCurrentLine.charAt(0)=='#') continue; // these are comment lines
                StringTokenizer st = new StringTokenizer(sCurrentLine);
                while (st.hasMoreTokens()) {
                    vertices[0] = st.nextToken();
                    vertices[1] = st.nextToken();
                }
                totalEdges++;
                Edge edge = new Edge(Integer.parseInt(vertices[0]),Integer.parseInt(vertices[1]));
                sampleEdge(edge);
                addEdgeToBlackEdge(edge);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sampleEdge(Edge edge) {
        if(Math.random() <= q) {
            edgeReservoir.add(edge);
            addEdgeToReservoir(edge);
        }
    }

    public void addEdgeToReservoir(Edge edge) {

        int u =edge.u, v = edge.v;
        VertexInfo vl2Info,ul2Info;
        vl2Info = res2map.get(v);
        ul2Info = res2map.get(u);

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

    public void addEdgeToBlackEdge(Edge edge){
        int u=edge.u, v = edge.v;
        boolean isBlackCounted=false;
        if(vertexReservoir.contains(u)){
            if(res2map.containsKey(v)) {
                isBlackCounted = true;
                blackEdgeCount++;
                HashSet<Integer> l2Neighbors = res2map.get(v).neighbors;
                if(res3map.get(u)==null) {
                    res3map.put(u,new VertexInfo(u));
                }
                if(res3map.get(v)==null) {
                    res3map.put(v,new VertexInfo(v));
                }
                res3map.get(u).neighbors.add(v);
                res3map.get(v).neighbors.add(u);

                HashSet<Integer> l3Neighbors = res3map.get(u).neighbors;
                HashSet<Integer> tempSet = new HashSet<Integer>(l3Neighbors);
                tempSet.retainAll(l2Neighbors);
                if(tempSet.size()>0)
                    addTriangle(tempSet, u,v);
                tempSet = null; //force garabage collector
            }
        }

        if(vertexReservoir.contains(v)){
            if(res2map.containsKey(u)) {
                if(!isBlackCounted){
                    blackEdgeCount++;
                }

                HashSet<Integer> l2Neighbors = res2map.get(u).neighbors;
                if(res3map.get(u)==null) {
                    res3map.put(u,new VertexInfo(u));
                }
                if(res3map.get(v)==null) {
                    res3map.put(v,new VertexInfo(v));
                }
                res3map.get(u).neighbors.add(v);
                res3map.get(v).neighbors.add(u);

                HashSet<Integer> l3Neighbors = res3map.get(v).neighbors;
                HashSet<Integer> tempSet = new HashSet<Integer>(l3Neighbors);
                tempSet.retainAll(l2Neighbors);
                if(tempSet.size()>0)
                    addTriangle(tempSet, u,v);
                tempSet=null;//force garabage collector
            }
        }
    }

//    public double seeDuplicatesInReservoirs(){
//        double duplicates = 0;
//        for (Map.Entry<Integer,VertexInfo> entry : res2map.entrySet()) {
//            HashSet<Integer> neighbors1 = entry.getValue().neighbors;
//            if(res3map.get(entry.getKey())!=null){
//                HashSet<Integer> neighbors2 = res3map.get(entry.getKey()).neighbors;
//                HashSet<Integer> tempSet = new HashSet<Integer>(neighbors2);
//                tempSet.retainAll(neighbors1);
//                duplicates +=tempSet.size();
//            }
//        }
//        return duplicates;
//    }
//
//    public int getEndCount(){
//        Iterator<Edge> itr = edgeReservoir.iterator();
//        int count=0;
//        while(itr.hasNext()){
//            Edge e = itr.next();
//            int u = e.u, v=e.v;
//            if(res3map.get(u) !=null && res3map.get(v) !=null)
//            {
//            HashSet<Integer> list1 = res3map.get(u).neighbors;
//            HashSet<Integer> list2 = res3map.get(v).neighbors;
//                HashSet<Integer> temp = new  HashSet<Integer>(list2);
//                temp.retainAll(list1);
//                count+=temp.size();
//            }
//        }
//        return count;
//    }


    public static void main(String args[]) {
        //constants for running the comparison
        String filename="com-amazon_undirected.txt";
        int totalVertices = 334863;
        int actualCount=667129; //this is used only for the error % calculation
        int iterations=5;

        double[] ns = {1,
                0.01, 0.05, 0.1, 0.15, 0.2,
                0.01, 0.05, 0.1, 0.15, 0.2,
                0.01, 0.05, 0.1, 0.15, 0.2,
                0.01, 0.05, 0.1, 0.15, 0.2,
                0.01, 0.05, 0.1, 0.15, 0.2};
        double[] ms = {1, 0.01, 0.01,0.01,0.01,0.01,
                0.05, 0.05,0.05,0.05,0.05,
                0.1,0.1,0.1,0.1,0.1,
                0.15 ,0.15 ,0.15 ,0.15 ,0.15 ,
                0.2,0.2,0.2,0.2,0.2};

        System.out.println("Multiple sampling single pass - PQ version : " + filename+"\n");

        for(int testcase=0;testcase<ns.length;testcase++){

            System.out.format("\n%-10s,%-10s,%-10s,%-15s,%-15s,%-15s,%-20s,%-30s,%-20s,%-20s",
                    "p","q","Vertices", "Edges","Black edges", "Total memory","Exact count", "Estimate","Error %","Time taken");
            double estimates[] = new double[iterations];
            for(int i=0;i<iterations;i++) {
                double startTime = System.currentTimeMillis();
                PQSampling r = new PQSampling(ns[testcase],ms[testcase],"graphs\\"+filename, totalVertices);
                r.sampleVertices();
                r.sampleEdges();
                estimates[i] = r.getEstimateCount();
                double endTime = System.currentTimeMillis();
                System.out.format("\n%-10s,%-10s,%-10s,%-15s,%-15s,%-15s,%-20s,%-30f,%-20s,%-20s",
                        r.p,r.q,r.vertexReservoir.size(), r.edgeReservoir.size(),r.blackEdgeCount, r.edgeReservoir.size()+r.blackEdgeCount,
                        r.triangleFormed.size(), estimates[i], 100*(estimates[i] - actualCount)/(double)actualCount  ,(endTime-startTime)/1000);

             //  System.out.println("\nCounted triangle:" + r.getEndCount());
             //   System.out.println("\nUnique count:" + r.triangleFormed.size());
              //  System.out.println("\nDupliacte count:" + r.seeDuplicatesInReservoirs());
                r.clearAll();
            }

            Arrays.sort(estimates);
            System.out.println("\nMedian:" + estimates[iterations/2]);
            double sum=0;
            for(int i=0;i<iterations;i++) {
                sum+=estimates[i];
            }
            System.out.println("Average:" + sum/iterations);
        }
    }

    public double getEstimateCount(){
        int uTriangleCount = this.triangleFormed.size();
        //    System.out.println("\nCounted traingls:" + triangleCount);
        // System.out.println("\nUique count:" + uTriangleCount);
        double estimate = uTriangleCount/(p*q);
        return estimate;
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
}
