package triest;

import java.util.*;

/**
 * Created by Neeraj on 10/5/2016.
 */
public class TriangleCounter {
    HashMap<Integer, VertexInfo> vertexMap = new HashMap<Integer, VertexInfo>();
    int globalCount =0;
    HashSet<Integer> vertexReservoir; // this is used only in vertex-edge sampling algorithm

    public void updateCounter(Edge edge, boolean isAdd) {
        int vertexU = edge.getVertexU();
        int vertexV = edge.getVertexV();

        VertexInfo vertexUInfo=vertexMap.get(vertexU);
        VertexInfo vertexVInfo=vertexMap.get(vertexV);

        if(isAdd){
            if(vertexUInfo==null){
                vertexUInfo = new VertexInfo();
                vertexMap.put(vertexU,vertexUInfo);
            }
            if(vertexVInfo==null){
                vertexVInfo = new VertexInfo();
                vertexMap.put(vertexV,vertexVInfo);
            }
            // add neighbor information and update triangle counts
            vertexUInfo.getNeighbors().add(vertexV);
            vertexVInfo.getNeighbors().add(vertexU);
        }
        else{
            vertexUInfo.getNeighbors().remove(vertexV);
            vertexVInfo.getNeighbors().remove(vertexU);
        }

        //get all common neighboring vertices of u and v
        TreeSet<Integer> copyTreeSet = new TreeSet<Integer>();
        copyTreeSet.addAll(vertexUInfo.getNeighbors());
        copyTreeSet.retainAll(vertexVInfo.getNeighbors());

        Iterator<Integer> iter = copyTreeSet.iterator();

        while(iter.hasNext()){
            Integer vertex = iter.next();
            VertexInfo vertexCInfo = vertexMap.get(vertex);
            if(isAdd){
                vertexCInfo.incrementCount();
                vertexUInfo.incrementCount();
                vertexVInfo.incrementCount();
                globalCount++;
            }
            else{
                vertexCInfo.decrementCount();
                vertexUInfo.decrementCount();
                vertexVInfo.decrementCount();
                globalCount--;
            }
        }

//        if(!isAdd){
//            //These lines are to remove vertices when there are no neighbors, so that we don't run out of memory
//            if(vertexUInfo.getNeighbors().size()==0) vertexMap.remove(vertexU);
//            if(vertexVInfo.getNeighbors().size()==0) vertexMap.remove(vertexV);
//        }
    }

    public int getGlobalCount() {
        return globalCount;
    }

    public void clearGlobalCount(){
        globalCount=0;
    }

    public void setVertexReservoir(HashSet<Integer> vertexReservoir) {
        this.vertexReservoir = vertexReservoir;
    }

    class VertexInfo{
        int triangleCounts;
        TreeSet<Integer> neighbors;

        public VertexInfo(){
            triangleCounts=0;
            neighbors = new TreeSet<Integer>();
        }

        public TreeSet<Integer> getNeighbors(){
            return neighbors;
        }

        public void incrementCount(){
            triangleCounts++;
        }

        public void decrementCount(){
            triangleCounts--;
        }
    }
}
