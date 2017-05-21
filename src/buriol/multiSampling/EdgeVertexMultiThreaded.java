package buriol.multiSampling;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Neeraj on 10/16/2016.
 */
public class EdgeVertexMultiThreaded implements Callable<Integer> {
    HashSet<Integer> vertexReservoir = new HashSet<Integer>();
    ArrayList<Edge> edgeReservoir = new ArrayList<Edge>();
    String inputFile;
    public int totalVertices, triangleCount=0, totalEdges=0, vreservoirCapcity, eReservoirCapacity,blueEdges=0, repetititons=0;
    HashSet<String> triangleFormed;

    HashMap<Integer,VertexInfo> res1map = new HashMap<Integer,VertexInfo>();
    HashMap<Integer,VertexInfo> res2map= new HashMap<Integer,VertexInfo>();
    HashMap<Integer,VertexInfo> res3map= new HashMap<Integer,VertexInfo>();

    ArrayList<String> fileBuffer = new ArrayList<String>();

    public EdgeVertexMultiThreaded(int repetititons, String s) {
        vreservoirCapcity = 1;
        eReservoirCapacity = 1;
        inputFile=s;
        this.repetititons = repetititons;
    }

    public void clearAll(){
        edgeReservoir.clear();
        vertexReservoir.clear();
        res1map.clear();
        res2map.clear();
        res3map.clear();
        triangleFormed.clear();
        totalVertices=0; triangleCount=0; totalEdges=0;blueEdges=0;

    }

    public void sampleVertices(){
        //get the file and parse the edges
        try {
            String sCurrentLine;
            Iterator<String> itr = fileBuffer.iterator();
            /*First pass: Get the vertex set*/
            String[] vertices = new String[2];
            while (itr.hasNext()) {
                sCurrentLine = itr.next();
                //vertices = sCurrentLine.split("\t");
                if(sCurrentLine.charAt(0)=='#') continue; // these are comment lines
                StringTokenizer st = new StringTokenizer(sCurrentLine);
                while (st.hasMoreTokens()) {
                    vertices[0] = st.nextToken();
                    vertices[1] = st.nextToken();
                }
                vertexReservoir.add(Integer.parseInt(vertices[0]));
                vertexReservoir.add(Integer.parseInt(vertices[1]));
            }
            totalVertices = vertexReservoir.size();
            /**Sample vertexReservoirCapacity number of vertices from vertexReservoir*/
            if(vertexReservoir.size()>vreservoirCapcity){
                List<Integer> list = new ArrayList<Integer>(vertexReservoir);
                int limit = list.size();
                vertexReservoir.clear();
                for(int i=0;i<vreservoirCapcity;i++){
                    int randomIndex = new Random().nextInt(limit-i);
                    vertexReservoir.add(list.get(randomIndex));
                    list.remove(randomIndex);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // br.close();
        }
    }


    private void sampleEdges() {
        BufferedReader br;
        try {
            String sCurrentLine;
            Iterator<String> itr = fileBuffer.iterator();
            /*First pass: Get the vertex set*/
            String[] vertices = new String[2];
            while (itr.hasNext()) {
                sCurrentLine = itr.next();
                //vertices = sCurrentLine.split("\t");
                if(sCurrentLine.charAt(0)=='#') continue; // these are comment lines
                StringTokenizer st = new StringTokenizer(sCurrentLine);
                while (st.hasMoreTokens()) {
                    vertices[0] = st.nextToken();
                    vertices[1] = st.nextToken();
                }
                totalEdges++;
                sampleEdge(new Edge(Integer.parseInt(vertices[0]),Integer.parseInt(vertices[1])));
                // edgeReservoir.add(new Edge(Integer.parseInt(vertices[0]),Integer.parseInt(vertices[1])));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // br.close();
        }

    }

    private void sampleEdge(Edge edge) {
        if(totalEdges<=eReservoirCapacity){
            edgeReservoir.add(edge);
        }
        else{
            int random = (new Random().nextInt(totalEdges));
            //Boolean isHead = <reservoirCapacity  ;
            if(random<eReservoirCapacity){
                int randomIndex = new Random().nextInt(eReservoirCapacity);
                edgeReservoir.remove(randomIndex);
                edgeReservoir.add(randomIndex,edge);
                // true;
            }
            else{
                // System.out.println("Failed to sample edge at t=" + t + "u,v = " + edge.getVertexU()+","+edge.getVertexV());

            }
        }
        //return false;
    }



    @Override
    public Integer call() {
        System.out.println("Starting Thread :" + Thread.currentThread().getName() );
        loadFile();
        int count=0;
        for(int i=0;i<repetititons;i++){
            sampleVertices();
            sampleEdges();
            getCounts();
            count+=triangleFormed.size();
            clearAll();
        }
        System.out.println("Thread name:" + Thread.currentThread().getName() + " .Count: " + count);
        return count;
    }

    public static void main(String args[]){

        //constants for running the comparison
        String filename="oregon1_010331.txt";
        int repetitions = 50000;
        int threads=50;
        int eachThreadReptition = 1000;
        int totalVertices = 10670;
        int totalEdges = 22002;

        int rep = repetitions;
        System.out.println("creating service");
        ExecutorService service = Executors.newFixedThreadPool(threads);
        Callable myCallable=null;
        List<Future<Integer>> futureList = new ArrayList<Future<Integer>>();

        while(rep>0){

            myCallable = new EdgeVertexMultiThreaded(rep>=eachThreadReptition?eachThreadReptition:rep,"graphs\\"+filename);
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
        System.out.format("\nEstimate %-20s", Double.toString(getEstimateCount(count/repetitions,totalVertices,totalEdges)));
    }

    public static double getEstimateCount(double average, int totalEdges, int totalVertices){
        double estimate = (((double)totalEdges*(double)totalVertices)*average/(3));
        return estimate;
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

    public double getEstimateCount(){
        int uTriangleCount = this.triangleFormed.size();
        double estimate = ((((double)totalEdges*(double)totalVertices))/(vreservoirCapcity*eReservoirCapacity))*uTriangleCount/3;
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

        try {
            String sCurrentLine;
            Iterator<String> it = fileBuffer.iterator();
            /*First pass: Get the vertex set*/
            String[] vertices = new String[2];
            while (it.hasNext()) {
                sCurrentLine = it.next();
                //vertices = sCurrentLine.split("\t");
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
                    int l2vertex = v;
                    int l1vertex = u;

                    if(res2map.containsKey(l2vertex)) {
                        VertexInfo vl3Info, ul3Info;
                        //add all vertices from edgeReservoir
                        vl3Info = res3map.get(v);
                        ul3Info = res3map.get(u);
                        isBlueCounted = true;
                        blueEdges++;
                        if (vl3Info == null) {
                            vl3Info = new VertexInfo(v);
                            res3map.put(v, vl3Info);
                        }
                        if (ul3Info == null) {
                            ul3Info = new VertexInfo(u);
                            res3map.put(u, ul3Info);
                        }

                        vl3Info.neighbors.add(u);
                        ul3Info.neighbors.add(v);

                        HashSet<Integer> neighbors = res2map.get(l2vertex).neighbors;
                        Iterator<Integer> neIter = neighbors.iterator();
                        while (neIter.hasNext()){
                            int currNeigh = neIter.next();

                            if( res3map.get(currNeigh)!=null){
                                HashSet<Integer> l3neighbors = res3map.get(currNeigh).neighbors;

                                if(l3neighbors.contains(l1vertex)){
                                    ArrayList<Integer> temp = new ArrayList<Integer>();
                                    temp.add(l1vertex);
                                    temp.add(l2vertex);
                                    temp.add(currNeigh);
                                    Collections.sort(temp);
                                    Iterator<Integer> itr = temp.iterator();
                                    StringBuffer str = new StringBuffer("");
                                    while(itr.hasNext()){
                                        str.append(Integer.toString(itr.next())).append(" ");
                                    }
                                    triangleFormed.add(str.toString());
                                    triangleCount++;
                                }
                            }

                        }
                    }
                }

                if(res1map.containsKey(v)){
                    int l2vertex = u;
                    int l1vertex = v;

                    if(res2map.containsKey(l2vertex)) {
                        VertexInfo vl3Info, ul3Info;
                        //add all vertices from edgeReservoir
                        vl3Info = res3map.get(v);
                        ul3Info = res3map.get(u);
                        if(!isBlueCounted ) // this check is required so that we do not add the edge count twice
                            blueEdges++;
                        if (vl3Info == null) {
                            vl3Info = new VertexInfo(v);
                            res3map.put(v, vl3Info);
                        }
                        if (ul3Info == null) {
                            ul3Info = new VertexInfo(u);
                            res3map.put(u, ul3Info);
                        }

                        vl3Info.neighbors.add(u);
                        ul3Info.neighbors.add(v);

                        HashSet<Integer> neighbors = res2map.get(l2vertex).neighbors;
                        Iterator<Integer> neIter = neighbors.iterator();
                        while (neIter.hasNext()){
                            int currNeigh = neIter.next();

                            if( res3map.get(currNeigh)!=null){
                                HashSet<Integer> l3neighbors = res3map.get(currNeigh).neighbors;

                                if(l3neighbors.contains(l1vertex)){
                                    ArrayList<Integer> temp = new ArrayList<Integer>();
                                    temp.add(l1vertex);
                                    temp.add(l2vertex);
                                    temp.add(currNeigh);
                                    Collections.sort(temp);
                                    Iterator<Integer> itr = temp.iterator();
                                    StringBuffer str = new StringBuffer("");
                                    while(itr.hasNext()){
                                        str.append(Integer.toString(itr.next())).append(" ");
                                    }
                                    triangleFormed.add(str.toString());
                                    triangleCount++;
                                }
                            }

                        }
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // br.close();
        }
    }

    class Edge{
        int u,v;
        public Edge(int i, int j){
            u=i;v=j;
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
