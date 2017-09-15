package neighborhood;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Neeraj on 9/9/2017.
 */
public class MultiSampleSinglePass {
    HashMap<Integer, HashSet<EdgeInfo>> redEdges = new HashMap<Integer, HashSet<EdgeInfo>>();
    HashMap<Integer, HashSet<EdgeInfo>> blueEdges= new HashMap<Integer, HashSet<EdgeInfo>>();
    HashSet<String> triangleFormed = new HashSet<String>();
    double p,q;
    String inputFile;
    int totalEdges, triangleCount=0, redEdgesCount, blueEdgesCount, repeats=0;

    /*******/
    BufferedWriter bw = null;
    FileWriter fw = null;

    public MultiSampleSinglePass(double p, double q, String inputFile){
        this.p =p;
        this.q=q;
        this.inputFile = inputFile;
        /******/
        try {
            File file = new File("output_graphs.txt");
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

        }
    }

    public void clearAll(){
        redEdges.clear();
        blueEdges.clear();
        p=q=0;
        totalEdges=0;
        triangleFormed.clear();
        triangleCount=0;
        redEdgesCount=0;blueEdgesCount=0;
    }

    public void sampleEdges(){
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
                //Red level edges
                if(Math.random()<p){
                    sampleRedEdge(edge,totalEdges);
                    redEdgesCount++;
                }
                if(isNeighbor(edge) && Math.random()<q){
                    sampleBlueEdge(edge,totalEdges);
                    blueEdgesCount++;
                }
                checkTriangle(edge,totalEdges);
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sampleRedEdge(Edge edge, int timeStamp){
        int u =edge.u, v = edge.v;
        HashSet<EdgeInfo> vNeighbors,uNeighbors;
        vNeighbors = redEdges.get(v);
        uNeighbors = redEdges.get(u);

        if(vNeighbors==null) {
            vNeighbors = new HashSet<EdgeInfo>();
            redEdges.put(v,vNeighbors);
        }
        if(uNeighbors==null) {
            uNeighbors = new HashSet<EdgeInfo>();
            redEdges.put(u,uNeighbors);
        }
        vNeighbors.add( new EdgeInfo(u,timeStamp));
        uNeighbors.add(new EdgeInfo(v,timeStamp));
    }

    public void sampleBlueEdge(Edge edge, int timeStamp){
        int u =edge.u, v = edge.v;
        HashSet<EdgeInfo> vNeighbors,uNeighbors;
        vNeighbors = blueEdges.get(v);
        uNeighbors = blueEdges.get(u);

        if(vNeighbors==null) {
            vNeighbors = new HashSet<EdgeInfo>();
            blueEdges.put(v,vNeighbors);
        }
        if(uNeighbors==null) {
            uNeighbors = new HashSet<EdgeInfo>();
            blueEdges.put(u,uNeighbors);
        }
        vNeighbors.add( new EdgeInfo(u,timeStamp));
        uNeighbors.add(new EdgeInfo(v,timeStamp));
    }

    public void checkTriangle(Edge edge, int timeStamp){
        //u in red and v in blue
        if( redEdges.get(edge.u)!=null && blueEdges.get(edge.v)!=null ){
            HashSet<EdgeInfo> uNeighbors = redEdges.get(edge.u);
            HashSet<EdgeInfo> vNeighbors = blueEdges.get(edge.v);
            Set<EdgeInfo> commonUVertices = uNeighbors.stream().filter(o -> vNeighbors.contains(o)&& timeStamp > o.timeStamp).collect(Collectors.toSet());
            Set<EdgeInfo> commonVVertices = vNeighbors.stream().filter(o -> uNeighbors.contains(o)&& timeStamp > o.timeStamp).collect(Collectors.toSet());

            Iterator<EdgeInfo> itr = commonUVertices.iterator();
            while(itr.hasNext()) {
                EdgeInfo uNeighbor = itr.next();
                Set<EdgeInfo> finalList = commonVVertices.stream().filter(o -> (o.vertex==uNeighbor.vertex && uNeighbor.timeStamp < o.timeStamp)).collect(Collectors.toSet());
                addTriangle(finalList ,edge.u,edge.v);
            }
        }

        //v in red and u in blue
        if( redEdges.get(edge.v)!=null && blueEdges.get(edge.u)!=null ){
            HashSet<EdgeInfo> vNeighbors = redEdges.get(edge.v);
            HashSet<EdgeInfo> uNeighbors = blueEdges.get(edge.u);
            Set<EdgeInfo> commonVVertices = vNeighbors.stream().filter(o -> uNeighbors.contains(o)&& timeStamp > o.timeStamp).collect(Collectors.toSet());
            Set<EdgeInfo> commonUVertices = uNeighbors.stream().filter(o -> vNeighbors.contains(o)&& timeStamp > o.timeStamp).collect(Collectors.toSet());

            Iterator<EdgeInfo> itr = commonVVertices.iterator();
            while(itr.hasNext()) {
                EdgeInfo vNeighbor = itr.next();
                Set<EdgeInfo> finalList = commonUVertices.stream().filter(o -> (o.vertex==vNeighbor.vertex && vNeighbor.timeStamp < o.timeStamp)).collect(Collectors.toSet());
                addTriangle(finalList ,edge.u,edge.v);
            }
        }
    }

    public void addTriangle(Set<EdgeInfo> vertices, int u, int v){
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

            String triangelName = smallest+","+middle+","+largest;
            if(triangleFormed.contains(triangelName)){
                repeats++;
            }
            triangleFormed.add(triangelName);
           // System.out.println(triangelName+"\n");
            try {
                bw.write(smallest+","+middle+","+largest+"\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
            triangleCount++;
        }
    }



    public boolean isNeighbor(Edge edge){
        return (redEdges.get(edge.u)!=null || redEdges.get(edge.v)!=null);
    }


    public static void main(String args[]) {
        //constants for running the comparison
        String filename="com-live-journal_simplified.txt";
        //int totalVertices = 317080;
        int actualCount=177820130; //this is used only for the error % calculation
        int iterations=1;

        double[] p = { 0.01, 0.05, 0.1, 0.15, 0.2,
                0.01, 0.05, 0.1, 0.15, 0.2,
                0.01, 0.05, 0.1, 0.15, 0.2,
                0.01, 0.05, 0.1, 0.15, 0.2,
                0.01, 0.05, 0.1, 0.15, 0.2};
        double[] q = { 0.01, 0.01,0.01,0.01,0.01,
                0.05, 0.05,0.05,0.05,0.05,
                0.1,0.1,0.1,0.1,0.1,
                0.15 ,0.15 ,0.15 ,0.15 ,0.15 ,
                0.2,0.2,0.2,0.2,0.2};


        System.out.println("Neighborhood Multiple sampling single pass - PQ version : " + filename+"\n");
        ArrayList<String> outputTable = new ArrayList<String>();
        for(int testcase=0;testcase<p.length;testcase++){
            HashMap<Double,String> currOutputMap = new HashMap<Double,String> ();
            System.out.format("\n%-10s,%-10s,%-20s,%-20s,%-15s,%-15s,%-20s,%-30s,%-20s\n",
                    "p","q", "Red Edges","Blue edges", "Total memory","Exact count", "Estimate","Error %","Time taken");
            double estimates[] = new double[iterations];
            for(int i=0;i<iterations;i++) {
                double startTime = System.currentTimeMillis();
                MultiSampleSinglePass r = new MultiSampleSinglePass(p[testcase],q[testcase],"graphs\\"+filename);
                r.sampleEdges();
                estimates[i] = r.getEstimateCount();
                double endTime = System.currentTimeMillis();
                System.out.println("Actual count :" + r.triangleCount);
                System.out.println("Repeats :" + r.repeats);
                String op = String.format("%-10s,%-10s,%-20s,%-20s,%-15s,%-15s,%-20s,%-30s,%-20s",
                        r.p,r.q, r.redEdgesCount,r.blueEdgesCount, r.redEdgesCount+r.blueEdgesCount,r.triangleFormed.size(), estimates[i], 100*(estimates[i] - actualCount)/(double)actualCount,(endTime-startTime)/1000);

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

        System.out.format("%-10s,%-10s,%-20s,%-20s,%-15s,%-15s,%-20s,%-30s,%-20s,%-20s\n",
                "p","q", "Red Edges","Blue edges", "Total memory","Exact count", "Estimate","Error %","Time taken","Average");
        Iterator<String> itr  = outList.iterator();
        while(itr.hasNext()){
            System.out.println(itr.next());
        }
        System.out.println("################################################################");
    }

    public double getEstimateCount(){
        int uTriangleCount = this.triangleFormed.size();
        double estimate = uTriangleCount/(p*q);
        return estimate;
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
