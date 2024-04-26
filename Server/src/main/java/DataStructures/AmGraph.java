package DataStructures;

import IPSwitch.Router;
import IPSwitch.Switch;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class AmGraph {
    private final Map<String, Node> nodesByCityName;
    private final Map<Node, Map<Node, Double>> adjacencyMatrix;

    public AmGraph(HashMap<String, Node> nodesByCityName) {
        this.adjacencyMatrix = new HashMap<>();
        this.nodesByCityName = nodesByCityName;

        for (String cityName : nodesByCityName.keySet()) {
            addNode(cityName);
        }
    }

    /**
     * Method to add a node to the graph using the city name String
     * @param cityName The name of the city to add to the graph, String
     */
    public void addNode(String cityName) {
        Node node = nodesByCityName.get(cityName);
        adjacencyMatrix.put(node, new Hashtable<>());
    }

    // bidirectional edges

    /**
     * Method to add an edge between two nodes in the graph
     * @param cityName1 the name of the first city
     * @param cityName2 the name of the second city
     */
    public void addEdge(String cityName1, String cityName2) {
        Node node1 = nodesByCityName.get(cityName1);
        Node node2 = nodesByCityName.get(cityName2);
        double distance = node1.getDistanceTo(node2);
        adjacencyMatrix.get(node1).put(node2, distance);
        adjacencyMatrix.get(node2).put(node1, distance);
    }

    /**
     * Method to get the edge between two nodes in the graph
     * @param cityName1 the name of the first city
     * @param cityName2 the name of the second city
     * @return the distance between the two cities using haveersine formula
     */
    public double getEdge(String cityName1, String cityName2) {
        Node node1 = nodesByCityName.get(cityName1);
        Node node2 = nodesByCityName.get(cityName2);
        return adjacencyMatrix.get(node1).get(node2);
    }

    public Set<Node> getNodes() {
        return adjacencyMatrix.keySet();
    }

    public void removeNode(String cityName) {
        Node nodeToRemove = nodesByCityName.get(cityName);

        // Remove all edges from the node
        adjacencyMatrix.remove(nodeToRemove);

        // Remove all edges to the node
        for (Map<Node, Double> edges : adjacencyMatrix.values()) {
            edges.remove(nodeToRemove);
        }
    }

    public void printGraph() {
        for (HashMap.Entry<Node, Map<Node, Double>> entry : adjacencyMatrix.entrySet()) {
            Node node1 = entry.getKey();
            String nodeName1 = node1 instanceof Switch ? ((Switch) node1).getCityName() : ((Router) node1).getName();
            System.out.print(nodeName1 + ": ");
            for (Map.Entry<Node, Double> innerEntry : entry.getValue().entrySet()) {
                Node node2 = innerEntry.getKey();
                String nodeName2 = node2 instanceof Switch ? ((Switch) node2).getCityName() : ((Router) node2).getName();
                Double distance = innerEntry.getValue();
                System.out.print("(" + nodeName2 + ", " + distance + ") ");
            }
            System.out.println();
        }
    }

}
