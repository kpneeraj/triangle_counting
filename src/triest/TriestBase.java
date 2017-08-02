package triest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Created by Neeraj on 10/5/2016.
 */
public class TriestBase {
    int reservoirCapacity, t=0;
    ArrayList<Edge> reservoir;
    TriangleCounter counter;
    String inputFile;

    public TriestBase(int reservoirCapacity, String inputFile){
        this.reservoirCapacity = reservoirCapacity;
        this.inputFile = inputFile;
        counter = new TriangleCounter();
        reservoir = new ArrayList<Edge>();
    }

    public int getReserviorCapacity(){
        return reservoirCapacity;
    }

    /*
        1: S ← ∅, t ← 0, τ ← 0 (done in constructor)
        2: for each element (+, (u, v)) from Σ do
        3: t ← t + 1
        4: if SampleEdge((u, v), t) then
        5: S ← S ∪ {(u, v)}
        6: UpdateCounters(+, (u, v)) */

    public void execute(){

        String[] vertices= new String[2];
        //read the edge file
        BufferedReader br = null;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(inputFile));
            while ((sCurrentLine = br.readLine()) != null) {
              //  double startTime = System.currentTimeMillis();
                if(sCurrentLine.charAt(0)=='#') continue; // these are comment lines
                StringTokenizer st = new StringTokenizer(sCurrentLine);
                while (st.hasMoreTokens()) {
                    vertices[0] = st.nextToken();
                    vertices[1] = st.nextToken();
                }
                t++;
                Edge edge = new Edge(Integer.parseInt(vertices[0]),Integer.parseInt(vertices[1]));
                if(sampleEdge(edge)){
                    reservoir.add(edge);
                    counter.updateCounter(edge,true);
                }
              //  double end = System.currentTimeMillis();
                // if( (end-startTime)/1000 >= 0.1)
              //  System.out.println("Time for proccessing edge:" + (end-startTime)/1000);
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

    private boolean sampleEdge(Edge edge) {
      //  double startTime = System.currentTimeMillis();
        if(t<=reservoirCapacity){
            return true;
        }
        else{
            int random = (new Random().nextInt(t));
            if(random<reservoirCapacity){
                counter.updateCounter(reservoir.get(random),false);
                reservoir.remove(random);
                reservoir.add(random,edge);
                return true;
            }
        }
      //  double end = System.currentTimeMillis();
      //  System.out.println("Time for sampling edge :" + (end-startTime)/1000);
        return false;
    }

    public double getTriangleCount() {
        double p = 0;
        if (t >= 3){
            p = ((double)reservoirCapacity/t)*((double)(reservoirCapacity-1)/(t-1)) *((double)(reservoirCapacity-2)/(t-2));
            p = Math.min(p, 1.0);
            return (double)counter.getGlobalCount()/p;
        } else {
            return 0; // no triangles possible;
        }
    }

    public int getAbsoluteTriangleCOunt(){
        return counter.getGlobalCount();
    }

    public static void main(String args[]){
        int medianCount =1;
        String filename = "com-dblp_undirected.txt";
        int[] memory = {26758,
                65562,
                204009,
                512802,
                921138,
                1434877};

        System.out.println("Triest Base - " + filename + "\n");

        for(int trial = 0;trial<memory.length;trial++) {
            double estimates[] = new double[medianCount];
            System.out.format("%-20s,%-20s,%-20s,%-20s,%-20s", "Memory", "Exact count", "Estimate","Error %","Time taken");
            for (int i = 0; i < medianCount; i++) {
                double startTime = System.currentTimeMillis();
                TriestBase triestBase = new TriestBase(memory[trial],"graphs\\"+filename);
                triestBase.execute();
                estimates[i] = triestBase.getTriangleCount();
                double endTime = System.currentTimeMillis();
                System.out.format("\n%-20d,%-20d,%-20f,%-20s,%-20s", triestBase.getReserviorCapacity(), triestBase.getAbsoluteTriangleCOunt(), estimates[i],100*( 2224385-estimates[i])/(double)2224385,(endTime-startTime)/1000);
            }

            Arrays.sort(estimates);
            System.out.println("\nMedian:" + estimates[medianCount / 2]);
        }
    }
}
