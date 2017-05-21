package triest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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

    public void execute(){
        String[] vertices= new String[2];

        //read the edge file

        BufferedReader br = null;
        try {
            String sCurrentLine;
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
//                if(vertices[0].equals("1")||vertices[1].equals("1"))
//                    System.out.println(vertices[0]+","+vertices[1]);
                Edge edge = new Edge(Integer.parseInt(vertices[0]),Integer.parseInt(vertices[1]));
                if(sampleEdge(edge)){
                    reservoir.add(edge);
                    counter.updateCounter(edge,true);
                 //   System.out.println("Adding edge" + edge.toString());
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


        /*
        1: S ← ∅, t ← 0, τ ← 0 (done in constructor)
        2: for each element (+, (u, v)) from Σ do
        3: t ← t + 1
        4: if SampleEdge((u, v), t) then
        5: S ← S ∪ {(u, v)}
        6: UpdateCounters(+, (u, v))*/
    }

    private boolean sampleEdge(Edge edge) {
        if(t<=reservoirCapacity){
            return true;
        }
        else{
            int random = (new Random().nextInt(t));
            //Boolean isHead = <reservoirCapacity  ;
            if(random<reservoirCapacity){
                int randomIndex = new Random().nextInt(reservoirCapacity);
              //  System.out.println("deleting edge " + edge.toString());
                counter.updateCounter(reservoir.get(randomIndex),false);
                reservoir.remove(randomIndex);
                reservoir.add(randomIndex,edge);
                return true;
            }
            else{
               // System.out.println("Failed to sample edge at t=" + t + "u,v = " + edge.getVertexU()+","+edge.getVertexV());

            }
        }
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
}
