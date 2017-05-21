package triest;

/**
 * Created by Neeraj on 10/5/2016.
 */
public class Edge {
    int vertexU, vertexV;
    public Edge(int u, int v){
        this.vertexU = Math.min(u,v);
        this.vertexV = Math.max(u,v);
    }

    public int getVertexU(){
        return vertexU;
    }

    public int getVertexV(){
        return vertexV;
    }

    public String toString(){
        return vertexU + ", "+ vertexV;
    }

    public boolean equals(Object obj){
        if(obj instanceof  Edge){
            if(((Edge) obj).vertexU == this.vertexU  && (((Edge) obj).vertexV==this.vertexV))
                return true;
        }
        return false;
    }
}
