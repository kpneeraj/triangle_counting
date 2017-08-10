package buriol.multiSampling;

import java.io.*;
import java.util.*;

/**
 * Created by Neeraj on 10/16/2016.
 */
public class MultiSamplingMultiPass {
    HashSet<Integer> vertexReservoir = new HashSet<Integer>();
    ArrayList<Edge> edgeReservoir = new ArrayList<Edge>();
    String inputFile;
    int totalVertices, triangleCount=0, totalEdges=0, vreservoirCapcity, eReservoirCapacity,blueEdges=0;
    HashSet<String> triangleFormed;

    HashMap<Integer,VertexInfo> res1map = new HashMap<Integer,VertexInfo>();
    HashMap<Integer,VertexInfo> res2map= new HashMap<Integer,VertexInfo>();
    HashMap<Integer,VertexInfo> res3map= new HashMap<Integer,VertexInfo>();

    //ArrayList<String> fileBuffer = new ArrayList<String>();


    public MultiSamplingMultiPass(int i, int i1, String s, int totalVertices) {
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
            else {
                int random = (new Random().nextInt(i));
                if(random<vreservoirCapcity){
                    vertexReservoirList.remove(random);
                    vertexReservoirList.add(random,i);
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
        else {
            int random = (new Random().nextInt(totalEdges));
            if(random<eReservoirCapacity){
                edgeReservoir.remove(random);
                edgeReservoir.add(random,edge);
            }
        }
    }

    public static void main(String args[]){
        //constants for running the comparison
        String filename="as-skitter_undirected.txt";
        int totalVertices = 1696415; //1,806,067,135   4,173,724,142
        double actualTriangleCount =28769868; //this is used only for the error % calculation
        int iterations=5;

        int[] ns = {17007,
                84075,
                169436,
                254541,
                339566,

                17192,
                84502,
                170230,
                254944,
                339635,

                17103,
                85031,
                169404,
                254932,
                338399,

                17151,
                85139,
                170816,
                254110,
                338495,

                16803,
                84791,
                169304,
                253910,
                338334};
        int[] ms = { 110996,
                110931,
                110751,
                111190,
                111086,

                555005,
                553972,
                553729,
                554807,
                554758,

                1108626,
                1111017,
                1109780,
                1109176,
                1109459,

                1663134,
                1665772,
                1664424,
                1663646,
                1663424,

                2217721,
                2220126,
                2219619,
                2219002,
                2218330};

        System.out.println("Multi pass multi sampling - " + filename + "\n");

        for(int testcase=0;testcase<ns.length;testcase++){
            System.out.println("\n\nTEst case result for n=" + ns[testcase] + " and m="+ms[testcase]);
            MultiSamplingMultiPass r = new MultiSamplingMultiPass(ns[testcase],ms[testcase],"graphs\\"+filename, totalVertices);
            System.out.println("Multiple sampling algorithm:");
            //System.out.format("\n%-20s%-20s%-20s%-20s%-20s%-20s%-40s%-20s%-20s", "Iteration", "Vertex memory(n)", "Edge memory(m)","Black edges sampled", "Total size", "Exact count", "Estimate","Error %","Time taken");
            System.out.format("\n%-20s,%-20s,%-20s,%-20s,%-20s,%-40s,%-20s,%-20s",  "Vertex memory(n)", "Edge memory(m)","Black edges sampled", "Total size", "Exact count", "Estimate","Error %","Time taken");

            double estimates[] = new double[iterations];
            for(int i=0;i<iterations;i++) {
                double startTime = System.currentTimeMillis();
                r.sampleVertices();
                double time1 = System.currentTimeMillis();
                r.sampleEdges();
                double time2 = System.currentTimeMillis();
                double timeParsingFile = time2-time1;
                r.getCounts();
                estimates[i] = r.getEstimateCount();
                double endTime = System.currentTimeMillis();
                System.out.format("\n%-20s,%-20s,%-20s,%-20s,%-20s,%-40s,%-20s,%-20s", r.vreservoirCapcity, r.eReservoirCapacity, r.blueEdges, (r.eReservoirCapacity + r.blueEdges) ,  r.triangleFormed.size(), estimates[i],100*( actualTriangleCount-estimates[i])/(double)actualTriangleCount,(endTime-startTime)/1000);
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
}
