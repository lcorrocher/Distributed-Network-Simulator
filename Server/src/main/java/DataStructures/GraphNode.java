package DataStructures;

/**
 * Class that represents a node in our graph.
 * Currently, the graph is not used in the network, this is purely visual.
 */
public class GraphNode {
    private String locationName;
    private int location;

    public GraphNode(String locationName, int location){
        this.locationName = locationName;
        this.location = location;
    }

    public String getLocationName() {
        return locationName;
    }

    public int getLocation(){
        return location;
    }
}
