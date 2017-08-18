package neighborhood;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Neeraj on 8/16/2017.
 */
public class NeighborhoodSampling {

    int totalEdges=0, memory=0,actualCount=0;
    String inputFile;
    ArrayList<Sampler> samplingInstances= new ArrayList<Sampler>();

    public NeighborhoodSampling(String inputFile, int size){
        this.inputFile = inputFile;
        this.memory = size;
        for(int i=0;i<memory;i++){
            samplingInstances.add(new Sampler());
        }
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
                for(Sampler s : samplingInstances){
                    s.addEdge(Integer.parseInt(vertices[0]),Integer.parseInt(vertices[1]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getEstimateCount(){
        double sum=0;
        for(Sampler s : samplingInstances){
            if(s.isTriangle) {
                actualCount++;
                sum+=s.c*s.t;
            }

        }
        return 1.0*sum/memory;

    }

    public static void main(String args[]){
        //constants for running the comparison
        String filename="com-dblp_simplified.txt";
        int totalVertices = 317080;
        int totalEdges = 1049866;
        int actualTriangleCount= 2224385; //this is used only for error % calculation
        int iterations=1;

        int[] memory = {12376,
                20469,
                30944,
                40747,
                50318,

                58869,
                86370,
                119359,
                152304,
                183740,

                114498,
                153380,
                203520,
                250749,
                295178,

                170486,
                216389,
                274446,
                330007,
                384455,

                224229,
                275683,
                340952,
                406363,
                465393};
        System.out.println("Neighborhood sampling- " + filename + "\n");
        ArrayList<String> outputTable = new ArrayList<String>();
        for(int testcase=0;testcase<memory.length;testcase++){
            HashMap<Double,String> currOutputMap = new HashMap<Double,String> ();
            System.out.println("\nTEst case result for m="+memory[testcase]);

            System.out.println("Multiple sampling algorithm with fixed p and q:");
            System.out.format("%-10s,%-10s,%-10s,%-20s,%-20s\n",
                    "M","Actual count","Estimate", "Error %", "Time taken");

            double estimates[] = new double[iterations];
            for(int i=0;i<iterations;i++) {
                double startTime = System.currentTimeMillis();
                NeighborhoodSampling r = new NeighborhoodSampling("graphs\\"+filename, memory[testcase]/3);
                r.sampleEdges();
                estimates[i] = r.getEstimateCount();
                double endTime = System.currentTimeMillis();

                String op = String.format("%-10s,%-10s,%-10s,%-20s,%-20s",
                        r.memory,
                        r.actualCount,
                        estimates[i],
                        100*( actualTriangleCount-estimates[i])/(double)actualTriangleCount,(endTime-startTime)/1000);
                System.out.println(op);
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

        System.out.format("\n%-10s,%-10s,%-10s,%-20s,%-20s,%-20s\n",
                "M", "Actual count","Estimate", "Error %", "Time taken","Average");
        Iterator<String> itr  = outList.iterator();
        while(itr.hasNext()){
            System.out.println(itr.next());
        }
        System.out.println("################################################################");
    }


    class Edge{
        int u,v;
        public Edge(int i, int j){
            if(i<j){
                u=i;v=j;
            }
            else {
                v=i;u=j;
            }
        }
    }

    class Sampler {
        public Edge r1,r2,r3;
        public int c=0,t=0;
        boolean isTriangle=false;
        public void addFirstLevelEdge(int u, int v){
            r1= new Edge(u,v);
            r2=null;
            c=0;
            isTriangle=false;
        }

        public boolean isNeighbor(int i, int j) {

            if(r1==null) return false;

            if ( i == r1.u || i == r1.v  ||  j==r1.u || i==r1.v ) {
                c++;
                return true;
            }
            return false;
        }

        public void addSecondLevelEdge(int i, int j){
            r2= new Edge(i,j);
        }

        public boolean doesCompleteTriangle(int i, int j){
            if(r1==null || r2==null ) return false;

            if(((r1.u == i || r1.v == i) && (r2.u == j || r2.v == j)) ||
                    ((r1.u == j || r1.v == j) && (r2.u == i || r2.v == i))){
                isTriangle=true;
                r3=new Edge(i,j);
            }
            return isTriangle;
        }

        public void addEdge(int i, int j){
            this.t++;
            if (Math.random()<= 1.0/t){
                addFirstLevelEdge(i,j);
            }
            else {
                if(isNeighbor(i,j)){
                    c++;
                    if (Math.random()<= 1.0/c){
                        addSecondLevelEdge(i,j);
                    }
                    else{
                        doesCompleteTriangle(i,j);
                    }
                }
            }
        }
    }
}
