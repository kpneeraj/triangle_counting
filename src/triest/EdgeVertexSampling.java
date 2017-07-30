package triest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/****
 * Created by Neeraj on 10/12/2016.
 ****/
public class EdgeVertexSampling {
    HashSet<Integer> vertexReservior;
    String inputFile;
    int vertexReservoirCapacity, edgeReservoirCapacity,t, totalVertices, totalEdges;
    ArrayList<Edge> edgeReservoir;
    TriangleCounter counter;

    public EdgeVertexSampling(int vertexReservoirCapacity, int edgeReservoirCapacity, String inputFile){
        vertexReservior = new HashSet<Integer>();
        this.inputFile = inputFile;
        this.vertexReservoirCapacity = vertexReservoirCapacity;
        this.edgeReservoirCapacity = edgeReservoirCapacity;
        counter = new TriangleCounter();
        edgeReservoir = new ArrayList<Edge>();
        counter.setVertexReservoir(vertexReservior);
    }

    public void execute(){
        String[] vertices= new String[2];
        //read the edge file
        BufferedReader br = null;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(inputFile));
            /*First pass: Get the vertex set*/
            while ((sCurrentLine = br.readLine()) != null) {
                //vertices = sCurrentLine.split("\t");
                if(sCurrentLine.charAt(0)=='#') continue; // these are comment lines
                StringTokenizer st = new StringTokenizer(sCurrentLine);
                while (st.hasMoreTokens()) {
                    vertices[0] = st.nextToken();
                    vertices[1] = st.nextToken();
                }
                vertexReservior.add(Integer.parseInt(vertices[0]));
                vertexReservior.add(Integer.parseInt(vertices[1]));
            }
            totalVertices = vertexReservior.size();
            /**Sample vertexReservoirCapacity number of vertices from vertexReservoir*/
            if(vertexReservior.size()>vertexReservoirCapacity){
                List<Integer> list = new ArrayList<Integer>(vertexReservior);
                int limit = list.size();
                vertexReservior.clear();
                for(int i=0;i<vertexReservoirCapacity;i++){
                    int randomIndex = new Random().nextInt(limit-i);
                    vertexReservior.add(list.get(randomIndex));
                    list.remove(randomIndex);
                }
            }

            /*Second pass: Get the edge set*/
            br = new BufferedReader(new FileReader(inputFile));
            while ((sCurrentLine = br.readLine()) != null) {
                //vertices = sCurrentLine.split("\t");
                if(sCurrentLine.charAt(0)=='#') continue; // these are comment lines
                StringTokenizer st = new StringTokenizer(sCurrentLine);
                while (st.hasMoreTokens()) {
                    vertices[0] = st.nextToken();
                    vertices[1] = st.nextToken();
                }
                t++;
                Edge edge = new Edge(Integer.parseInt(vertices[0]),Integer.parseInt(vertices[1]));
                if(sampleEdge(edge)){
                    edgeReservoir.add(edge);
                    counter.updateCounter(edge,true);
                }
            }
            totalEdges =t;

            counter.clearGlobalCount();
            /*Third pass: Get the edge set*/
            br = new BufferedReader(new FileReader(inputFile));
            while ((sCurrentLine = br.readLine()) != null) {
                //vertices = sCurrentLine.split("\t");
                if(sCurrentLine.charAt(0)=='#') continue; // these are comment lines
                StringTokenizer st = new StringTokenizer(sCurrentLine);
                while (st.hasMoreTokens()) {
                    vertices[0] = st.nextToken();
                    vertices[1] = st.nextToken();
                }
                Edge edge = new Edge(Integer.parseInt(vertices[0]),Integer.parseInt(vertices[1]));
                //check if either of the vertex is sampled in the the vertexReservoir
                if(vertexReservior.contains(Integer.parseInt(vertices[0])) || vertexReservior.contains(Integer.parseInt(vertices[1]))){
                    int indexNotPresent =0;
                    if(vertexReservior.contains(Integer.parseInt(vertices[0])) ){
                        indexNotPresent =1;
                    }
                    int vertexNotInVertexReservior = Integer.parseInt(vertices[indexNotPresent]);
                    TriangleCounter.VertexInfo vertexInfo= counter.vertexMap.get(vertexNotInVertexReservior);
                    if(vertexInfo!=null){ //we found a vertex-edge reservoir match
                        counter.updateCounter(edge,true);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public int getVertexReservoirCapacity(){
        return vertexReservoirCapacity;
    }

    public int getEdgeReservoirCapacity(){
        return edgeReservoirCapacity;
    }

    private boolean sampleEdge(Edge edge) {
        if(t<=edgeReservoirCapacity){
            return true;
        }
        else {
            int random = (new Random().nextInt(t));
            if(random<edgeReservoirCapacity){
                int randomIndex = new Random().nextInt(edgeReservoirCapacity);
                counter.updateCounter(edgeReservoir.get(randomIndex),false);
                edgeReservoir.remove(randomIndex);
                edgeReservoir.add(randomIndex,edge);
                return true;
            }
        }
        return false;
    }

    public double getTriangleCount() {
        return ( (double)getAbsoluteTriangleCOunt() )* totalEdges*totalVertices / (edgeReservoirCapacity*vertexReservoirCapacity) ;
    }

    public int getAbsoluteTriangleCOunt(){
        return counter.getGlobalCount()/3;
    }
}
