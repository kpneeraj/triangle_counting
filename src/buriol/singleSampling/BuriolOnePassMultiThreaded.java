package buriol.singleSampling;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Neeraj on 11/4/2016.
 */
public class BuriolOnePassMultiThreaded implements Callable<Integer> {


    ArrayList<Integer> vertices = new ArrayList<Integer>();
    ArrayList<String> fileBuffer = new ArrayList<String>();

    HashMap<String,ArrayList<Sample>> sampleMap = new HashMap<String,ArrayList<Sample>>();
    ArrayList<Sample> S = new ArrayList<Sample>();

    String inputFile;
    int iteration,triangleCount;

    public BuriolOnePassMultiThreaded( int iteration,String inputFile){
        this.inputFile = inputFile;
        this.iteration  = iteration;

    }

    public void loadFile(){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(inputFile));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                //vertices = sCurrentLine.split("\t");
                if (sCurrentLine.charAt(0) == '#') continue; // these are comment lines
                fileBuffer.add(sCurrentLine);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//old_man_code
    public int sampleTriangles(){
        /****
         SampleTriangleOnePass
         i ← 1
         for each edge e = (u, w) in the stream do
             Flip a coin. With probability 1/i do
                 a ← u; b ← w;
                 v ← Node uniformly chosen from V \ {a, b}
                 x ← false; y ← false
             end do
             if e = (a, v) then x ← true
             if e = (b, v) then y ← true
             i ← i + 1
         end for
         if x = true ∧ y = true then return β ← 1 else return β ← 0
        */

        /*
        fileBuffer.add(0,"");
        int s=0,A=0;
        int x=1,m=1,M=memory;
        String edge; StringTokenizer st;
        int a=0,b=0;
        for (A = 1; A < fileBuffer.size(); A++) {
            edge = fileBuffer.get(A);
            st = new StringTokenizer(edge);
            while (st.hasMoreTokens()) {
                a = Integer.parseInt(st.nextToken());
                b = Integer.parseInt(st.nextToken());
            }
            if(Math.random()<(1/m)){
            //if(A==x){
                s++;
                int v = getUniformVertexSample();
                Sample sample = new Sample(a,b,v,s);
                S.add(sample);
                insertIntoHash(getKey(a,v),sample);
                insertIntoHash(getKey(b,v),sample);
                //x = nextSample(1/m, x);
            }
            if(A==M){
                M=M*2;
                m=m*2;
                cleanHalfSampleSet();
            }
            //check Triangle in hashmap
            checkTriangle(a,b);
        }
        int beta=0;
        //count beta and return estimate
        for(int i=0;i<S.size();i++){
            if(S.get(i).s==2)beta++;
        }

        return beta;
*/


        fileBuffer.add(0,"");
        for(int itr=1;itr<iteration;itr++) {
            String edge; int e1=0,e2=0,a=0,b=0,v=0; boolean x=false,y=false;
            StringTokenizer st;
            for (int i = 1; i < fileBuffer.size(); i++) {
                edge = fileBuffer.get(i);
                st = new StringTokenizer(edge);
                while (st.hasMoreTokens()) {
                    e1 = Integer.parseInt(st.nextToken());
                    e2 = Integer.parseInt(st.nextToken());
                }
                if (canSample(i)) {
                    a = e1;
                    b = e2;
                    v = getUniformVertexSample();
                }
                if ((e1 == a && e2 == v) || (e1 == v && e2 == a)) x = true;
                if ((e1 == b && e2 == v) || (e1 == v && e2 == b)) y = true;
            }
            if (x && y) {
                triangleCount++;
            }
        }
        return triangleCount;
    }

    public boolean canSample(int m){
        return (Math.random()<(1/m));
    }

    public int nextSample(double p, int ki){
        int logPart = (int)Math.ceil(Math.log((Math.random()-1)/(p-1)) / Math.log(1-p) + 1);
        return ki + logPart;
    }

    public void checkTriangle(int a, int b){
        ArrayList<Sample> list = sampleMap.get(getKey(a,b));
        if(list!=null)
        for(int j=0;j<list.size();j++){
            list.get(j).count++;
        }
    }

    public String getKey(int a, int b){
        int min = Math.min(a,b);
        int max = Math.max(a,b);
        return min+"_"+max;
    }

    public void cleanHalfSampleSet(){
        int sampleSize = S.size();
        ArrayList<Sample> tobeDeleted = new ArrayList<Sample>();
        Random r =  new Random();

        for(int i=0;i<S.size();i++){
            if(r.nextInt(10)<=4){
                tobeDeleted.add(S.get(i));
            }
        }
        //remove from hashmap
        for(int i=0;i<tobeDeleted.size()-1;i++){
            Sample s = tobeDeleted.get(i);

            String key = getKey(s.a,s.v);
            ArrayList<Sample> list = sampleMap.get(key);
            for(int j=0;j<list.size();j++){
                if(list.get(j).equals(s)){
                    list.remove(j);break;
                }
            }
            key = getKey(s.b,s.v);
            list = sampleMap.get(key);
            for(int j=0;j<list.size();j++){
                if(list.get(j).equals(s)){
                    list.remove(j);break;
                }
            }
        }
        S.removeAll(tobeDeleted);
    }

    public int getUniformVertexSample(){
        return vertices.get(new Random().nextInt(vertices.size()));
    }

    public void insertIntoHash(String key, Sample sample){
        ArrayList<Sample> aL = sampleMap.get(key);
        if(aL==null){
            aL = new ArrayList<Sample>();
            sampleMap.put(key,aL);
        }
        aL.add(sample);
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Starting Thread :" + Thread.currentThread().getName() );
        loadFile();
        readVertices();
        int count=0;
      //  for(int i=0;i<memory;i++){
            count+=sampleTriangles();
            //clearAll();
      //  }
        System.out.println("Thread name:" + Thread.currentThread().getName() + " .Count: " + count);
        return count;
    }



    class Sample{
        public int a,b,v,s,count;
        public Sample(int a, int b, int v,int s){
            this.a=a;
            this.b=b;
            this.v=v;
            this.s=s;
            count=0;
        }

        public boolean equals(Sample other){
            return this.s==other.s;
        }

    }


    public void readVertices(){
        HashSet<Integer> allVertices = new HashSet<Integer>();
        //get the file and parse the edges
        try {
            String sCurrentLine;
            Iterator<String> itr = fileBuffer.iterator();
            /*First pass: Get the vertex set*/
            String[] currVertices = new String[2];
            while (itr.hasNext()) {
                sCurrentLine = itr.next();
                //vertices = sCurrentLine.split("\t");
                if(sCurrentLine.charAt(0)=='#') continue; // these are comment lines
                StringTokenizer st = new StringTokenizer(sCurrentLine);
                while (st.hasMoreTokens()) {
                    currVertices[0] = st.nextToken();
                    currVertices[1] = st.nextToken();
                }

                int u = Integer.parseInt(currVertices[0]);
                int v = Integer.parseInt(currVertices[1]);

                if(!allVertices.contains(u)){
                    allVertices.add(u);
                    vertices.add(u);
                }

                if(!allVertices.contains(v)){
                    allVertices.add(v);
                    vertices.add(v);
                }
                allVertices.add(Integer.parseInt(currVertices[0]));
                allVertices.add(Integer.parseInt(currVertices[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // br.close();
        }
    }



    public static void main(String args[]){
       //constants for running the comparison
        String filename="facebook_combined.txt";
        int repetitions = 50000;
        int threads=50;
        int eachThreadReptition = 1000;
        int totalVertices = 4039;
        int totalEdges = 88234;

        int rep = repetitions;
        System.out.println("creating service");
        ExecutorService service = Executors.newFixedThreadPool(threads);
        Callable myCallable=null;
        List<Future<Integer>> futureList = new ArrayList<Future<Integer>>();

        while(rep>0){
            myCallable = new BuriolOnePassMultiThreaded(rep>=eachThreadReptition?eachThreadReptition:rep,"graphs\\"+filename);
            //submit Callable tasks to be executed by thread pool
            Future<Integer> future = service.submit(myCallable);
            //add Future to the list, we can get return value using Future
            futureList.add(future);
            rep = rep-eachThreadReptition;
        }
        // wait for all tasks to complete before continuing
        int count=0;

        try {
            for (Future<Integer> f : futureList)
            {
                count+=f.get();
            }
        }
        catch(Exception e){

        }

        System.out.println("Completed");
        service.shutdown();
        System.out.println("\nTotal Triangle Count:" + count);
        System.out.println("\nAverage:" + count/repetitions);
        System.out.format("\nEstimate %-20s", Double.toString(getEstimateCount((double)count/repetitions,totalVertices,totalEdges)));

//        BuriolOnePassMultiThreaded  sampler = new BuriolOnePassMultiThreaded("graphs\\roadNet-PA.txt",100000);
//        int i = sampler.sampleTriangles();
//        System.out.println(i);
    }

    public static double getEstimateCount(double average, int totalEdges, int totalVertices){
        double estimate = (((double)totalEdges*(double)totalVertices)*average);
        return estimate;
    }




}
