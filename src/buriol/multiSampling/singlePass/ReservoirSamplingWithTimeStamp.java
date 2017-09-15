package buriol.multiSampling.singlePass;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Neeraj on 8/14/2017.
 */
public class ReservoirSamplingWithTimeStamp {


    HashSet<Integer> vertexReservoir = new HashSet<Integer>();
    ArrayList<Edge> edgeReservoir = new ArrayList<Edge>();
    String inputFile;
    int blackEdgeCount, totalVertices, triangleCount=0, totalEdges=0,vreservoirCapcity=0, eReservoirCapacity=0;
    HashSet<String> triangleFormed = new HashSet<String>();
    HashMap<Integer,HashSet<EdgeInfo>> res2map= new HashMap<Integer,HashSet<EdgeInfo>>();
    HashMap<Integer,HashSet<EdgeInfo>> res3map= new HashMap<Integer,HashSet<EdgeInfo>>();

    public ReservoirSamplingWithTimeStamp(int i, int i1, String s, int totalVertices) {
        vreservoirCapcity = i;
        eReservoirCapacity = i1;
        inputFile=s;
        this.totalVertices = totalVertices;
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
                if(sCurrentLine.charAt(0)=='#') continue; // these are comment lines
                StringTokenizer st = new StringTokenizer(sCurrentLine);
                while (st.hasMoreTokens()) {
                    vertices[0] = st.nextToken();
                    vertices[1] = st.nextToken();
                }
                totalEdges++; //this is also used as the timestamp for the current edge
                Edge edge = new Edge(Integer.parseInt(vertices[0]),Integer.parseInt(vertices[1]));
                sampleEdge(edge,totalEdges);
                addEdgeToBlackEdge(edge,totalEdges);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sampleEdge(Edge edge, int timeStamp) {
        if(totalEdges<=eReservoirCapacity){
            edgeReservoir.add(edge);
            addEdgeToReservoir(edge,totalEdges);
        }
        else {
            int random = (new Random().nextInt(totalEdges));
            if(random<eReservoirCapacity){
                Edge e = edgeReservoir.remove(random);
                removeEdgeFromReservoir(e);
                //remove corresponding black edges from level3 edge
                removeCorrespondingBackEdge(e);

                edgeReservoir.add(random,edge);
                addEdgeToReservoir(edge,totalEdges);
            }
        }
    }


    public void removeEdgeFromReservoir(Edge edge) {
        res2map.get(edge.u).remove(new EdgeInfo(edge.v,-1));
        if(res2map.get(edge.u).size()==0)
            res2map.remove(edge.u);

        res2map.get(edge.v).remove(new EdgeInfo(edge.u,-1));
        if(res2map.get(edge.v).size()==0)
            res2map.remove(edge.v);
    }

    public void removeCorrespondingBackEdge(Edge edge){
        if(res3map.get(edge.u) != null && (res2map.get(edge.u)==null || res2map.get(edge.u).size()==0)){
            blackEdgeCount = blackEdgeCount - res3map.get(edge.u).size();
            res3map.remove(edge.u);
        }
        if(res3map.get(edge.v) != null && (res2map.get(edge.v)==null || res2map.get(edge.v).size()==0)){
            blackEdgeCount = blackEdgeCount - res3map.get(edge.v).size();
            res3map.remove(edge.v);
        }
    }

    public void addEdgeToReservoir(Edge edge, int timeStamp) {

        int u =edge.u, v = edge.v;
        HashSet<EdgeInfo> vNeighbors,uNeighbors;
        vNeighbors = res2map.get(v);
        uNeighbors = res2map.get(u);

        if(vNeighbors==null){
            vNeighbors = new HashSet<EdgeInfo>();
            res2map.put(v,vNeighbors);
        }
        if(uNeighbors==null) {
            uNeighbors = new HashSet<EdgeInfo>();
            res2map.put(u,uNeighbors);
        }
        vNeighbors.add( new EdgeInfo(u,timeStamp));
        uNeighbors.add(new EdgeInfo(v,timeStamp));
    }

    public void addEdgeToBlackEdge(Edge edge,int timeStamp){
        int u=edge.u, v = edge.v;
        boolean isBlackCounted=false;
        if(vertexReservoir.contains(u)){
            if(res2map.containsKey(v)) {
                isBlackCounted = true;
                blackEdgeCount++;

                /** Notes to developer:
                 *  Add the black edge.
                 *  We add only one way, i.e from redEdge vertex to vertexReservoir for optimisation.
                 *  This is in reverse of the multiPass optimisation technique.
                 * **/
                HashSet<EdgeInfo> vNeighbors = res3map.get(v);
                if(vNeighbors==null) {
                    vNeighbors = new HashSet<EdgeInfo>();
                    res3map.put(v,vNeighbors);
                }
                vNeighbors.add(new EdgeInfo(u,timeStamp));
            }
        }

        if(vertexReservoir.contains(v)){
            if(res2map.containsKey(u)) {
                if(!isBlackCounted){
                    blackEdgeCount++;
                }
                /** Notes to developer:
                 *  Add the black edge.
                 *  We add only one way, i.e from redEdge vertex to vertexReservoir for optimisation.
                 *  This is in reverse of the multiPass optimisation technique.
                 * **/
                HashSet<EdgeInfo> uNeighbors = res3map.get(u);
                if(uNeighbors==null) {
                    uNeighbors = new HashSet<EdgeInfo>();
                    res3map.put(u,uNeighbors);
                }
                uNeighbors.add(new EdgeInfo(v,timeStamp));
            }
        }
    }

    public void countTriangles() {
        Iterator<Edge> itr = edgeReservoir.iterator();
        while(itr.hasNext()){
            Edge e = itr.next();
            int u = e.u, v=e.v;
            int timeStamp = getTimeStampOfEdgeInRes2(u,v);

            HashSet<EdgeInfo> vNeighborSet = res3map.get(v);
            HashSet<EdgeInfo> uNeighborSet = res3map.get(u);

            if(vNeighborSet!=null && uNeighborSet!=null){
                HashSet<EdgeInfo> uNeighbors = getNeighborsAfterTimeStamp(uNeighborSet,timeStamp);
                HashSet<EdgeInfo> vNeighbors = getNeighborsAfterTimeStamp(vNeighborSet,timeStamp);

                vNeighbors.retainAll(uNeighbors);
                if(vNeighbors.size()>0)
                    addTriangle(vNeighbors, u,v);
            }
        }
    }

    private HashSet getNeighborsAfterTimeStamp(HashSet<EdgeInfo> neighbors, int timeStamp) {
        Set<EdgeInfo> e = neighbors.stream().filter(o -> o.timeStamp >= timeStamp).collect(Collectors.toSet());
        return (HashSet)e;
    }

    public int getTimeStampOfEdgeInRes2(int u, int v){
        HashSet<EdgeInfo> edgeSet = res2map.get(u);
        EdgeInfo vEdgeInfo = edgeSet.stream().filter(o -> o.equals(new EdgeInfo(v,-1))).collect(Collectors.toList()).iterator().next();
        return vEdgeInfo.timeStamp;
    }


    public static void main(String args[]) {
        //constants for running the comparison
        String filename="com-orkut_simplified.txt";
        int totalVertices = 3072441;
        int actualCount=627584181; //this is used only for the error % calculation
        int iterations=3;

        int[] n = {15000};

        int[] m = {420000};


        System.out.println("Reservoir sampling single pass with timestamp: " + filename+"\n");
        ArrayList<String> outputTable = new ArrayList<String>();
        for(int testcase=0;testcase<n.length;testcase++){
            HashMap<Double,String> currOutputMap = new HashMap<Double,String> ();
            System.out.format("\n%-10s,%-15s,%-15s,%-15s,%-20s,%-30s,%-20s,%-20s\n",
                    "Vertices", "Edges","Black edges", "Total memory","Exact count", "Estimate","Error %","Time taken");
            double estimates[] = new double[iterations];
            for(int i=0;i<iterations;i++) {
                double startTime = System.currentTimeMillis();
                ReservoirSamplingWithTimeStamp r = new ReservoirSamplingWithTimeStamp(n[testcase],m[testcase],"graphs\\"+filename, totalVertices);
                r.sampleVertices();
                r.sampleEdges();
                r.countTriangles();
                estimates[i] = r.getEstimateCount();
                double endTime = System.currentTimeMillis();

                String op = String.format("%-10s,%-15s,%-15s,%-15s,%-20s,%-30f,%-20s,%-20s",
                        r.vertexReservoir.size(), r.edgeReservoir.size(),r.blackEdgeCount, r.edgeReservoir.size()+r.blackEdgeCount,
                        r.triangleFormed.size(), estimates[i], 100*(estimates[i] - actualCount)/(double)actualCount  ,(endTime-startTime)/1000);
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
        System.out.println("################ Consolidated result till now: ##################");

        System.out.format("%-10s,%-15s,%-15s,%-15s,%-20s,%-30s,%-20s,%-20s,%-20s\n",
                "Vertices", "Edges","Black edges", "Total memory","Exact count", "Estimate","Error %","Time taken","Average");
        Iterator<String> itr  = outList.iterator();
        while(itr.hasNext()){
            System.out.println(itr.next());
        }
        System.out.println("################################################################");
    }

    public double getEstimateCount(){
        int uTriangleCount = this.triangleFormed.size();
        double estimate = ((((double)totalEdges*(double)totalVertices))/((double)vreservoirCapcity*eReservoirCapacity))*(uTriangleCount);
        return estimate;
    }

    public void addTriangle(HashSet<EdgeInfo> vertices, int u, int v){
        Iterator<EdgeInfo> itr = vertices.iterator();
        int w;
        while(itr.hasNext()){
            w = itr.next().vertex;
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

    class EdgeInfo implements Comparable {
        public int vertex , timeStamp;

        public EdgeInfo(int vertex, int timeStamp){
            this.vertex = vertex;
            this.timeStamp = timeStamp;
        }

        @Override
        public int compareTo(Object o) {
            int oTimeStamp = ((EdgeInfo)o).timeStamp;
            return this.timeStamp - oTimeStamp;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof EdgeInfo)) {
                return false;
            }
            EdgeInfo c = (EdgeInfo) o;
            return c.vertex == this.vertex;
        }

        @Override
        public int hashCode() {
            return this.vertex;
        }
    }



}
