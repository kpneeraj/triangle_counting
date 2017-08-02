package buriol.multiSampling;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Neeraj on 2/8/2017.
 */
public class MultiSamplingSinglePass {
    HashSet<Integer> vertexReservoir = new HashSet<Integer>();
    ArrayList<Edge> edgeReservoir = new ArrayList<Edge>();
    String inputFile;
    int blackEdgeCount, totalVertices, triangleCount=0, totalEdges=0, vreservoirCapcity, eReservoirCapacity;
    HashSet<String> triangleFormed = new HashSet<String>();

    //    HashMap<Integer,VertexInfo> res1map = new HashMap<Integer,VertexInfo>();
    HashMap<Integer,VertexInfo> res2map= new HashMap<Integer,VertexInfo>();
    HashMap<Integer,VertexInfo> res3map= new HashMap<Integer,VertexInfo>();

    //ArrayList<String> fileBuffer = new ArrayList<String>();


    public MultiSamplingSinglePass(int i, int i1, String s, int totalVertices) {
        vreservoirCapcity = i;
        eReservoirCapacity = i1;
        inputFile=s;
        this.totalVertices = totalVertices;
    }

    public void clearAll(){
        edgeReservoir.clear();
        vertexReservoir.clear();
        //   res1map.clear();
        res2map.clear();
        res3map.clear();
        triangleFormed.clear();
        triangleCount=0; totalEdges=0;
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
                Edge edge = new Edge(Integer.parseInt(vertices[0]),Integer.parseInt(vertices[1]));

                boolean hasSampled = sampleEdge(edge);
                addEdgeToBlackEdge(edge);
//                if(!hasSampled){ // add this edge to the black edge if posssible
//                    addEdgeToBlackEdge(edge);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean sampleEdge(Edge edge) {
        if(totalEdges<=eReservoirCapacity){
            edgeReservoir.add(edge);
            addEdgeToReservoir(edge);
            return true;
        }
        else {
            int random = (new Random().nextInt(totalEdges));
            if(random<eReservoirCapacity){
                Edge e = edgeReservoir.remove(random);
                //  removeTrianglesFormedWithEdge(e);
                removeEdgeFromReservoir(e);

                //remove corrsponding black edges from level3 edge
                removeCorrespondingBackEdge(e);

                edgeReservoir.add(random,edge);
                addEdgeToReservoir(edge);
                return true;
            }
        }
        return false;
    }

    public void removeTrianglesFormedWithEdge(Edge e){
        //assume the edge was a red edge. res2map - red edge

        HashSet<Integer> uNeighbors, vNeighbors, tempSet;
        if(res3map.get(e.u)!=null && res3map.get(e.v)!=null){
            uNeighbors = res3map.get(e.u).neighbors;
            vNeighbors = res3map.get(e.v).neighbors;
            tempSet = new HashSet<Integer>(uNeighbors);
            tempSet.retainAll(vNeighbors);
            removeTriangle(tempSet,e.u,e.v);
        }


        //assume the edge was a black edge. res2map - red edge
        //if u is th e vertex from the vertex reservoir
        if(res3map.get(e.u)!=null && res2map.get(e.v)!=null){

            uNeighbors = res3map.get(e.u).neighbors;
            vNeighbors = res2map.get(e.v).neighbors;
            tempSet = new HashSet<Integer>(uNeighbors);
            tempSet.retainAll(vNeighbors);
            removeTriangle(tempSet,e.u,e.v);

        }

        //if v is th e vertex from the vertex reservoir
        if(res3map.get(e.v)!=null && res2map.get(e.u)!=null) {
            uNeighbors = res3map.get(e.v).neighbors;
            vNeighbors = res2map.get(e.u).neighbors;
            tempSet = new HashSet<Integer>(uNeighbors);
            tempSet.retainAll(vNeighbors);
            removeTriangle(tempSet, e.u, e.v);
        }
    }

    public void removeTriangle(HashSet<Integer> vertices, int u, int v){
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

            triangleFormed.remove(smallest+","+middle+","+largest);
            //  triangleCount--;
        }
    }

    public void addEdgeToReservoir(Edge edge) {
        //  edgeReservoir.add(edge);

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

    public void removeEdgeFromReservoir(Edge edge) {
        res2map.get(edge.u).neighbors.remove(edge.v);
        if(res2map.get(edge.u).neighbors.size()==0)
            res2map.remove(edge.u);

        res2map.get(edge.v).neighbors.remove(edge.u);
        if(res2map.get(edge.v).neighbors.size()==0)
            res2map.remove(edge.v);
    }

    public void removeCorrespondingBackEdge(Edge edge){
        if(res3map.get(edge.u) != null){
            blackEdgeCount = blackEdgeCount - res3map.get(edge.u).neighbors.size();
            res3map.remove(edge.u);
        }
        if(res3map.get(edge.v) != null){
            blackEdgeCount = blackEdgeCount - res3map.get(edge.v).neighbors.size();
            res3map.remove(edge.v);
        }
    }

    public void addEdgeToBlackEdge(Edge edge){
        int u=edge.u, v = edge.v;
        boolean isBlueCounted=false;
        if(vertexReservoir.contains(u)){
            if(res2map.containsKey(v)) {
                isBlueCounted = true;
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
            }
        }

        if(vertexReservoir.contains(v)){
            if(res2map.containsKey(u)) {
                if(!isBlueCounted){
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
            }
        }
    }

    public double seeDuplicatesInReservoirs(){
        double duplicates = 0;
        for (Map.Entry<Integer,VertexInfo> entry : res2map.entrySet()) {
            HashSet<Integer> neighbors1 = entry.getValue().neighbors;
            if(res3map.get(entry.getKey())!=null){
                HashSet<Integer> neighbors2 = res3map.get(entry.getKey()).neighbors;
                HashSet<Integer> tempSet = new HashSet<Integer>(neighbors2);
                tempSet.retainAll(neighbors1);
                duplicates +=tempSet.size();
            }
        }
        return duplicates;
    }

    public static void main(String args[]) {
        //constants for running the comparison
        String filename="com-dblp_undirected.txt";
        int totalVertices = 317080;
        int iterations=1;

        int[] ns = {317080,10000, 50000, 75000, 100000, 200000};
        int[] ms = {1049866,75000, 100000, 500000, 1000000, 5000000};

        System.out.println("Multiple sampling algorithm - single pass" + filename+"\n");

        for(int testcase=0;testcase<ns.length;testcase++){
            System.out.println("\n\nTest case result for n=" + ns[testcase] + " and m="+ms[testcase]);
            MultiSamplingSinglePass r = new MultiSamplingSinglePass(ns[testcase],ms[testcase],"graphs\\"+filename, totalVertices);

            System.out.format("\n%-20s%-20s%-20s%-20s%-20s%-20s%-20s%-40s", "Iteration", "Vertex memory(n)", "Edge memory(m)","Blue edges sampled", "Total memory","Exact count", "Estimate","Time taken");

            double estimates[] = new double[iterations];
            for(int i=0;i<iterations;i++) {
                double startTime = System.currentTimeMillis();
                r.sampleVertices();
                //double time1 = System.currentTimeMillis();
                r.sampleEdges();
                double time2 = System.currentTimeMillis();
                // double timeParsingFile = time2-time1;
                //    r.getCounts();
                estimates[i] = r.getEstimateCount();
                double endTime = System.currentTimeMillis();
                System.out.format("\n%-20s%-20s%-20s%-20s%-20s%-20s%-20s%-40s", i, r.vreservoirCapcity, r.eReservoirCapacity, r.blackEdgeCount,( r.eReservoirCapacity+ r.blackEdgeCount), r.triangleCount, estimates[i],(endTime-startTime)/1000);
                System.out.println("\nCounted traingls:" + r.triangleCount);
                System.out.println("\nUnique count:" + r.triangleFormed.size());
                System.out.println("\nDupliacte count:" + r.seeDuplicatesInReservoirs());

                r.clearAll();
            }

            Arrays.sort(estimates);
            System.out.println("\nMedian:" + estimates[iterations/2]);
            double sum=0;
            for(int i=0;i<iterations;i++) {
                sum+=estimates[i];
            }
            //System.out.println("Average:" + sum/iterations);

        }
    }

    public double getEstimateCount(){
        int uTriangleCount = triangleCount;//this.triangleFormed.size();
        //    System.out.println("\nCounted traingls:" + triangleCount);
        // System.out.println("\nUique count:" + uTriangleCount);
        double estimate = ((((double)totalEdges*(double)totalVertices))/((double)vreservoirCapcity*eReservoirCapacity))*(uTriangleCount)/2;
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
