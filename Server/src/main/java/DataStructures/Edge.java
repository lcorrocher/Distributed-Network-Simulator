package DataStructures;

import java.util.Objects;

/**
 * Class that represents a connection between two nodes.
 */

public class Edge {
    private String src, dest;
    double distance;

    /**
     * Constructor for edge.
     * @param src The source node.
     * @param dest The destination node.
     * @param distance The distance between the nodes.
     */
    public Edge(String src, String dest, double distance){
        this.src = src;
        this.dest = dest;
        this.distance = distance;
    }

    public String getSrc() {
        return src;
    }

    public String getDest() {
        return dest;
    }

    public double getDistance(){
        return distance;
    }

    public String toString(){
        return "Source: " + src + " Destination: " + dest + " Distance: " + distance;
    }

    /**
     * Checks if the edges are equivalent, even if the source and destination are swapped.
     * @param edge The edge we want to check.
     * @return true if edges are equivalent.
     */
    public boolean isEqual(Edge edge){
        return Objects.equals(src, edge.src) && Objects.equals(dest, edge.dest) ||
                Objects.equals(src, edge.dest) && Objects.equals(dest, edge.src);
    }
}
