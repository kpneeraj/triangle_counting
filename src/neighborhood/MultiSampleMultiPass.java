package neighborhood;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

    /**
     * Created by Neeraj on 5/8/2017.
     */
public class MultiSampleMultiPass {

    HashMap<Integer,ArrayList<Integer>> greenEdges = new HashMap<Integer,ArrayList<Integer>>();
    HashMap<Integer,ArrayList<Integer>> redEdges = new HashMap<Integer,ArrayList<Integer>>();

    HashMap<Integer,Integer> degreeCount = new HashMap<Integer,Integer>();

    ArrayList<Edge> edgeReservoir = new ArrayList<Edge>();
    public String inputFile;
    public int totalEdges=0,eReservoirCapacity;
    public double q;
    public  double triangleCount=0, MOverm=0;

    public MultiSampleMultiPass(String inputFile,int  M, double q){
        this.eReservoirCapacity = M;
        this.inputFile = inputFile;
        this.q = q;
    }

    public void firstPass(){
        //sample green edges
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
                sampleGreenEdge(new Edge(Integer.parseInt(vertices[0]),Integer.parseInt(vertices[1])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //form the green adjacency list
        for(int i=0;i<edgeReservoir.size();i++){
            Edge e = edgeReservoir.get(i);

            int u =e.u, v = e.v;
            ArrayList<Integer> vList = greenEdges.get(v);
            ArrayList<Integer> uList = greenEdges.get(u);
            if(vList==null){
                vList= new ArrayList<Integer>();
                greenEdges.put(v,vList);
            }
            if(uList==null){
                uList= new ArrayList<Integer>();
                greenEdges.put(u,uList);
            }
            vList.add(u);
            uList.add(v);
            //also add the vertices to the degree and iCount map
            degreeCount.put(u,0);
            degreeCount.put(v,0);
        }
        MOverm = ((double)eReservoirCapacity)/(double)totalEdges;
        /**TODO : garbage collect the edgeReservoir, we dont need it anymore**/
        //edgeReservoir = null;
    }

    public void secondPass(){
        //sample red edges
        edgeReservoir = new ArrayList<Edge>();
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
                int u = Integer.parseInt(vertices[0]);
                int v = Integer.parseInt(vertices[1]);

                if(degreeCount.get(u)!=null){
                    int uDegree = degreeCount.get(u)+1;
                    degreeCount.put(u,uDegree);
                }
                if(degreeCount.get(v)!=null){
                    int vDegree = degreeCount.get(v)+1;
                    degreeCount.put(v,vDegree);
                }
                sampleRedEdge(new Edge(u,v));
            }

            //form the red adjacency list
            for(int i=0;i<edgeReservoir.size();i++) {
                Edge e = edgeReservoir.get(i);
                int u =e.u, v = e.v;
                ArrayList<Integer> vList = redEdges.get(v);
                ArrayList<Integer> uList = redEdges.get(u);
                if(vList==null){
                    vList= new ArrayList<Integer>();
                    redEdges.put(v,vList);
                }
                if(uList==null){
                    uList= new ArrayList<Integer>();
                    redEdges.put(u,uList);
                }
                vList.add(u);
                uList.add(v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void thirdPass(){
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
                int u = Integer.parseInt(vertices[0]);
                int v = Integer.parseInt(vertices[1]);
                checkAndAddTriagleCount(u,v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkAndAddTriagleCount(int u, int v) {
        //let u be in green and v in red
        if(greenEdges.get(u)!=null && redEdges.get(v)!=null) {
            ArrayList<Integer> temp = new ArrayList<>(greenEdges.get(u));
            temp.retainAll(redEdges.get(v));
            //now temp has all the common vertices whose degree and i need to be
            // taken into account for the scaling factor of that triangle
            for(int i=0;i<temp.size();i++){
               triangleCount+= getScaledFactor(temp.get(i));
            }
        }

        //let v be in green and u in red
        if(greenEdges.get(v)!=null && redEdges.get(u)!=null){
            ArrayList<Integer> temp = new ArrayList<>(greenEdges.get(v));
            temp.retainAll(redEdges.get(u));
            //now temp has all the common vertices whose degree and i need to be
            // taken into account for the scaling factor of that triangle
            for(int i=0;i<temp.size();i++){
                triangleCount+= getScaledFactor(temp.get(i));
            }
        }
    }

    public double getScaledFactor(int vertex){
     //    return 1;

        int d=degreeCount.get(vertex);
        double prob = 0;
        for(int i=0;i<d;i++){
            //double p_i = nCr(d-1,i).multiply()*Math.pow(MOverm,i+1) * Math.pow( 1-(MOverm), d-i-1);
            double powProducts = Math.pow(MOverm,i+1) * Math.pow( 1-(MOverm), d-i-1);
            BigDecimal tmp = new BigDecimal(nCr(d-1,i));
            tmp = tmp.multiply(new BigDecimal(powProducts));
            double p_i = tmp.doubleValue();
            double n_i = 1 -  Math.pow(1-(1/(double)d),i+1);
            prob += (p_i * n_i);
        }
        return 1/prob;

    }

    public BigInteger nCr(int n, int r){
        if(r==0 || n==r){
            return BigInteger.ONE;
        }
        BigInteger ans = BigInteger.ONE;
        //System.out.println("Computing nCr " + n + "C"+r);
        for (int i = 1; i <= r; i++) {
            ans = ans.multiply(BigInteger.valueOf(n-r+i)).divide(BigInteger.valueOf(i));
        }
        return ans;
    }

    public long factorial(int n){
        long ans=1;
        if(n==0 || n==1) return 1;
        for(int i=2;i<=n;i++){
            ans = ans*i;
        }
        return ans;
    }

    public void sampleRedEdge(Edge edge){
        int u = edge.u, v = edge.v;
        boolean isSampled = false;
        //if u is present in the greenSample
        if(!isSampled  && greenEdges.get(u)!=null){
            //for every vertex adjacent to u in green edges, try to sample the current red edge
            for(int i=0;i<greenEdges.get(u).size();i++){
                if(q>=Math.random()){
                    isSampled= true;
                    edgeReservoir.add(edge);
                    return;
                }
            }
        }
        //if v is present in the greenSample and the current edge is not already sampled
        if(!isSampled  && greenEdges.get(v)!=null){
            //for every vertex adjacent to u in green edges, try to sample the current red edge
            for(int i=0;i<greenEdges.get(v).size();i++){
                if(q>=Math.random()){
                    isSampled= true;
                    edgeReservoir.add(edge);
                    return;
                }
            }
        }
    }

    private void sampleGreenEdge(Edge edge) {
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

    class VertexInfo{
        int vertex;
        public HashSet<Integer> neighbors;
        public VertexInfo(int i){
            vertex=i;
            neighbors = new HashSet<Integer>();
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

    private void run() {
        this.firstPass();
       // System.out.println("First pass done ");
        this.secondPass();
        //System.out.println("Second pass done ");
        this.thirdPass();
       // System.out.println("Third pass done ");
    }

    public static void main(String args[]){
        String filename="com-dblp_undirected.txt";



        int testCases = 6;
        int[] M = {50000,
                30000,
                20000,
                20000,
                25000, 60000};
        double[] q = {0.01,
                0.02,
                0.015,
                0.02,0.04,0.009};

        System.out.format("\n%-20s%-20s%-20s%-20s%-20s", "M", "q", "Estimate","Estimate/3", "Time taken (s)" );
        for(int testcase=0;testcase<testCases;testcase++){
            MultiSampleMultiPass instance = new MultiSampleMultiPass("graphs\\"+filename, M[testcase], q[testcase]);
            double startTime = System.currentTimeMillis();
            instance.run();
            System.out.format("\n%-20s%-20s%-20s%-20s%-20s",instance.eReservoirCapacity,instance.q,instance.triangleCount,instance.triangleCount/3, ( System.currentTimeMillis()-startTime )/1000);
//            System.out.println("Total seconds:" + ( System.currentTimeMillis()-startTime )/1000);
//            System.out.println("M=" + instance.eReservoirCapacity + " q=" + instance.q);
//            System.out.println("Triangle estimate "+ instance.triangleCount);
//            System.out.println("Triangle estimate/3 "+ instance.triangleCount/3);
        }


    }
}

