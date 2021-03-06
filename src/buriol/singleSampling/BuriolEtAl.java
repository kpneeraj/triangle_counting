package buriol.singleSampling;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Neeraj on 11/4/2016.
 */
public class BuriolEtAl {

   // ArrayList<Integer> vertices = new ArrayList<Integer>();
    ArrayList<String> fileBuffer = new ArrayList<String>();

    HashMap<String,ArrayList<Sample>> sampleMap = new HashMap<String,ArrayList<Sample>>();
    ArrayList<Sample> S = new ArrayList<Sample>();

    String inputFile;
    int memory,triangleCount, vertexCount, beta;
    double estimate;
    int A=0;

    public BuriolEtAl(int memory, String inputFile, int vertexCount){
        this.inputFile = inputFile;
        this.memory = memory;
        this.vertexCount = vertexCount;
        //loadFileAndCollectVertices();
    }

    public int sampleTriangles(){
       // fileBuffer.add(0,"");
        int s=0;
        int x=1,m=1,M= memory;
        String edge; StringTokenizer st;
        int a=0,b=0;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(inputFile));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                edge = sCurrentLine;
                if(sCurrentLine.charAt(0)=='#') continue; // these are comment lines
                st = new StringTokenizer(edge);
                while (st.hasMoreTokens()) {
                    a = Integer.parseInt(st.nextToken());
                    b = Integer.parseInt(st.nextToken());
                }
                if(Math.random()<(1/(double)m)){
                    s++;
                    int v = getUniformVertexSample();
                    Sample sample = new Sample(a,b,v,s);
                    S.add(sample);
                    insertIntoHash(getKey(a,v),sample);
                    insertIntoHash(getKey(b,v),sample);
                }
                if(A==M){
                    M=M*2;
                    m=m*2;
                    cleanHalfSampleSet();
                }
                //check Triangle in hashmap
                checkTriangle(a,b);
                A++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int beta=0;
        //count beta and return estimate
        for(int i=0;i<S.size();i++){
            if(S.get(i).count==2)beta++;
        }
        //System.out.println("Size: " + S.size());
        this.beta = beta;
        //System.out.println("Triangles : "+ beta);
        this.estimate =  getEstimate(beta, A,vertexCount, S.size());
       // System.out.println("Estimate  = " +this.estimate);
        return beta;
    }

    public double getEstimate(int beta, int m, int n, int sampleSize){
        return ((double)beta /  sampleSize )*m*n;
    }

    public int getBeta(){
        return this.beta;
    }

    public double getEstimate(){
        return this.estimate;
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
        return (new Random().nextInt(vertexCount))+1 ;
    }

    public void insertIntoHash(String key, Sample sample){
        ArrayList<Sample> aL = sampleMap.get(key);
        if(aL==null){
            aL = new ArrayList<Sample>();
            sampleMap.put(key,aL);
        }
        aL.add(sample);
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

    public static void main(String args[]){
        //constants for running the comparison
        String filename="com-live-journal_simplified.txt";
        int vertexCount = 3997962;
        int actualTriangleCount= 177820130; //this is used only for error % calculation
        int iterations = 1;
        //int testCases = 7;
        int[] memory ={ 50000,
                        100000,
                        500000,
                        750000,
                        1000000,
                        2500000 };
        ArrayList<String> outputTable = new ArrayList<String>();
        System.out.println("Buriol et Al- " + filename + "\n");
        for(int testcase=0;testcase<memory.length;testcase++){
            HashMap<Double,String> currOutputMap = new HashMap<Double,String> ();
            int repetition = memory[testcase];
            double estimates[] = new double[iterations];
            System.out.format("\n%-20s,%-20s,%-20s,%-20s\n",  "Memory", "Actual Count" , "Estimate","Error %","Time taken");
            for(int i=0;i<iterations;i++){
                double startTime = System.currentTimeMillis();
                BuriolEtAl counter = new BuriolEtAl(repetition, "graphs\\"+filename, vertexCount);
                counter.sampleTriangles();
                double endTime = System.currentTimeMillis();
                estimates[i]=(counter.getEstimate());
                String op = String.format("%-20s,%-20s,%-20s,%-20s,%-20s", repetition,counter.beta,counter.estimate  ,  100*( actualTriangleCount-estimates[i])/(double)actualTriangleCount,(endTime-startTime)/1000);

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

        System.out.format("\n%-20s,%-20s,%-20s,%-20s,%-20s,%-20s\n", "Repetitions", "Actual Count" , "Estimate","Error %","Time taken","Average");

        Iterator<String> itr  = outList.iterator();
        while(itr.hasNext()){
            System.out.println(itr.next());
        }
        System.out.println("################################################################");
    }
}
