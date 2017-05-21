package triest;

import java.util.Arrays;

/**
 * Created by Neeraj on 10/5/2016.
 */
public class RunCounting {
    public static void main(String args[]){
       //call the triest exec method here
//       TriestBase triestBase = new TriestBase(12,"facebook\\3980.edges");
        System.out.println("Reservoir sampling\n");
        double estimates[] = new double[9];
        System.out.format("%20s%20s%20s%20s", "Iteration", "Memory", "Exact count", "Estimate");
        for(int i=0;i<9;i++){
            TriestBase triestBase = new TriestBase(3468118,"graphs\\com-livejournal.ungraph.txt");
            triestBase.execute();
            System.out.println();
            estimates[i] = triestBase.getTriangleCount();
            System.out.format("%20s%20d%20d%20f", i, triestBase.getReserviorCapacity(), triestBase.getAbsoluteTriangleCOunt(), triestBase.getTriangleCount());


        }

        Arrays.sort(estimates);
        System.out.println("\nMedian:" + estimates[5]);
        double sum=0;
        for(int i=0;i<9;i++) {
            sum+=estimates[i];
        }
        System.out.println("\nAverage:" + sum/9);

//        System.out.println();
//        System.out.println("Vertex-edge sampling");
        
//        System.out.format("%20s%20s%20s%20s%20s", "Iteration", "Vertex memory(n)", "Edge memory(m)", "Exact count/3", "Estimate");
//        for(int i =0;i<5;i++) {
//            EdgeVertexSampling edgeVertexSample = new EdgeVertexSampling(2000, 50000, "facebook\\facebook_combined.txt");
//            edgeVertexSample.execute();
//            System.out.println();
//            System.out.format("%20s%20d%20d%20d%20f", i, edgeVertexSample.getVertexReservoirCapacity(),edgeVertexSample.getEdgeReservoirCapacity(), edgeVertexSample.getAbsoluteTriangleCOunt(), edgeVertexSample.getTriangleCount());
//        }
  //      triestBase.execute();
       // System.out.println("i = " + i +" Triangle count = " + triestBase.getTriangleCount());
    //    System.out.println(" Triangle count = " + triestBase.getTriangleCount());

    }
}
