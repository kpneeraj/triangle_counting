package colorfulCounting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Neeraj on 9/3/2017.
 */
public class ColorfulCounting {

    HashMap<Integer,Integer> vertexColor= new HashMap<Integer,Integer>();
    ArrayList<HashMap<Integer,HashSet<Integer>>> edgeGroups = new ArrayList<HashMap<Integer,HashSet<Integer>>>();
    int colors, totalVertices,totalEdges=0,triangleCount=0,totalMemory=0;
    String inputFile;
    double p;
    Random rand = new Random();
    HashSet<String> triangleFormed = new HashSet<String>();

    public ColorfulCounting(double p, int totalVertices, String inputFile) {
        this.p=p;
        colors = (int) (1/p);
        this.totalVertices=totalVertices;
        this.inputFile=inputFile;
        for(int i=0;i<colors;i++){
            edgeGroups.add(new HashMap<Integer,HashSet<Integer>>());
        }
    }

    public void clearAll(){
        vertexColor.clear();
        edgeGroups.clear();
        colors=0;
    }

    /***
     * This method runs a for loop from 0 to n and mocking the vertex stream
     * and does a assigns a color to each of the vertices
     * */
    public void colorVertices(){
        for(int i=0;i<totalVertices;i++){
            vertexColor.put(i,rand.nextInt(colors));
        }
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
                Integer u = Integer.parseInt(vertices[0]), v= Integer.parseInt(vertices[1]);
                if(vertexColor.get(u) == vertexColor.get(v)){
                    totalMemory++;
                    sampleEdge(u,v);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sampleEdge(int u, int v) {
        HashMap<Integer,HashSet<Integer>> currEdgeGroup = edgeGroups.get(vertexColor.get(u));
        HashSet<Integer> vNeighbors,uNeighbors;
        vNeighbors = currEdgeGroup.get(v);
        uNeighbors = currEdgeGroup.get(u);

        if(vNeighbors==null) {
            vNeighbors = new HashSet<Integer>();
            currEdgeGroup.put(v,vNeighbors);
        }
        if(uNeighbors==null) {
            uNeighbors = new HashSet<Integer>();
            currEdgeGroup.put(u,uNeighbors);
        }
        vNeighbors.add(u);
        uNeighbors.add(v);
    }

    public void countTrianglesInAllGroups() {
        Iterator<HashMap<Integer,HashSet<Integer>>> itr = edgeGroups.iterator();
        while(itr.hasNext()){
               countTriangles(itr.next());
        }
    }

    public void countTriangles(HashMap<Integer, HashSet<Integer>> edges){
        Iterator<Integer> keysItr = edges.keySet().iterator();
        while(keysItr.hasNext()){
            Integer curr = keysItr.next();
            HashSet neighbors = edges.get(curr);
            Iterator<Integer> neighborIterator = neighbors.iterator();
            while(neighborIterator.hasNext()){
                Integer currNeighbor = neighborIterator.next();
                HashSet neighborsNeighbor = edges.get(currNeighbor);
                HashSet<Integer> e = (HashSet)neighborsNeighbor.stream().filter(o ->neighbors.contains(o)).collect(Collectors.toSet());
                addTriangle(e,curr,currNeighbor);
            }
        }
    }


    public static void main(String args[]) {
        //constants for running the comparison
        String filename="com-orkut_simplified.txt";
        int totalVertices = 3072441;
        int actualCount=627584181; //this is used only for the error % calculation
        int iterations=3;

        double[] ps = { 0.01, 0.025 ,0.05, 0.075, 0.1, 0.15, 0.2 };

        System.out.println("Colorful triangle counting - : " + filename+"\n");
        ArrayList<String> outputTable = new ArrayList<String>();
        for(int testcase=0;testcase<ps.length;testcase++){
            HashMap<Double,String> currOutputMap = new HashMap<Double,String> ();
            System.out.format("\n%-10s,%-10s,%-10s,%-15s,%-15s,%-15s\n",
                    "p", "Total memory","Exact count", "Estimate","Error %","Time taken");
            double estimates[] = new double[iterations];
            for(int i=0;i<iterations;i++) {
                double startTime = System.currentTimeMillis();
                ColorfulCounting r = new ColorfulCounting(ps[testcase],totalVertices,"graphs\\"+filename);
                r.colorVertices();
                r.sampleEdges();
                r.countTrianglesInAllGroups();
                estimates[i] = r.getEstimateCount();
                double endTime = System.currentTimeMillis();

                String op = String.format("%-10s,%-10s,%-10s,%-15s,%-15s,%-15s",
                        r.p,r.totalMemory, r.triangleFormed.size(), estimates[i], 100*(estimates[i] - actualCount)/(double)actualCount  ,(endTime-startTime)/1000);
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
        System.out.format("\n%-10s,%-10s,%-10s,%-15s,%-15s,%-15s,%-20s\n",
                "p", "Total memory","Exact count", "Estimate","Error %","Time taken","Average");

        Iterator<String> itr  = outList.iterator();
        while(itr.hasNext()){
            System.out.println(itr.next());
        }
        System.out.println("################################################################");
    }

    public double getEstimateCount(){
        int uTriangleCount = this.triangleFormed.size();
        double estimate = uTriangleCount/(p*p);
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
}
