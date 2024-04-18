package DataStructures;

import IPSwitch.NetworkUI;
import utils.FileUtils;

import java.io.IOException;
import java.util.*;

/**
 * Visually represents the topology of a IPSwitch.Network.
 */
public class Graph {
    private Set<String> blueNodes = new HashSet<>();
    private ArrayList<Edge> edges;
    private ArrayList<GraphNode> nodes;

    /**
     * Constructor
     * @param path The topology.txt file path.
     * @throws IOException Catches errors in opening the file.
     */
    public Graph(String path) throws IOException {
        ArrayList<Edge> edges = new ArrayList<>();
        ArrayList<GraphNode> nodes = new ArrayList<>();

        // Reads our topology file.
        List<String[]> topology = FileUtils.readFile(path);

        // Creates our map.
        Map<String[], double[]> topologyMap = createDistancesMap(topology, blueNodes);

        // Using a set to remove duplicate nodes.
        // Uses a boolean and a for loop to remove duplicate edges. SMELLY.
        HashSet<String> nodeNames = new HashSet<>();
        for (String[] key: topologyMap.keySet()){
            double[] distances = topologyMap.get(key);
            for (int i = 0; i < key.length-1; i++){
                String src = key[i];
                String dest = key[i+1];
                double dist = distances[i];
                if (dist == 0){
                    dist = (new Random().nextDouble() + 1) * 200;
                }

                Edge edge = new Edge(src, dest, (int) dist);
                boolean alreadyContains = false;
                for (Edge e: edges){
                    if (e.isEqual(edge)){
                        alreadyContains = true;
                    }
                }
                if (!alreadyContains){
                    edges.add(edge);
                }

                nodeNames.add(src);
            }
        }

        for (String location: nodeNames){
            nodes.add(new GraphNode(location, 1));
        }

        this.edges = edges;
        this.nodes = nodes;
    }

    /**
     * Method that converts the topology into a map.
     * @param fullTopology The topology as read from the file.
     * @return Map where the key is a route, and the value stored, is an array of the distances between the nodes.
     */

    private Map<String[], double[]> createDistancesMap(List<String[]> fullTopology, Set<String> blueNodes) {
        Map<String[], double[]> output = new HashMap<>();

        for (String[] route : fullTopology) {
            int len = route.length + 1;
            String[] locationsInOrder = new String[len];
            double[] distancesInOrder = new double[len];

            // Split fresnoToNyc|2122 into source, destination, and distance
            String srcDestInformation = route[0];
            String[] parts = srcDestInformation.split("\\|");
            String[] srcDest = parts[0].split("To");
            String src = srcDest[0];
            String dest = srcDest[1];

            double d = Double.parseDouble(parts[1]);

            locationsInOrder[0] = src.toUpperCase();
            locationsInOrder[len - 1] = dest.toUpperCase();

            // Mark source nodes
            blueNodes.add(src.toUpperCase());

            // get the distance
            distancesInOrder[0] = d;

            int numRemaining = len - 2;
            int index = 1;

            while (numRemaining > 0) {
                String switchInfo = route[index];
                String[] info = switchInfo.split("\\|");
                String locationName = info[0];
                double distance = Double.parseDouble(info[2]);
                locationsInOrder[index] = locationName;
                distancesInOrder[index] = distance;
                index++;
                numRemaining--;
            }
            output.put(locationsInOrder, distancesInOrder);
        }
        return output;
    }



    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public ArrayList<GraphNode> getNodes() {
        return nodes;
    }


    /**
     * Finds the shortest path between two nodes using Dijkstra's algorithm.
     * @param source The source node.
     * @param destination The destination node.
     * @return The shortest path as a list of GraphNodes.
     */
    public List<GraphNode> shortestPath(GraphNode source, GraphNode destination) {
        Map<GraphNode, Double> distances = new HashMap<>();
        Map<GraphNode, GraphNode> previous = new HashMap<>();
        Heap heap = new Heap(nodes.size()); // Initialise heap with the number of nodes

        for (GraphNode node : nodes) {
            distances.put(node, Double.POSITIVE_INFINITY);
            previous.put(node, null);
        }

        distances.put(source, 0.0);
        heap.insert(new AbstractMap.SimpleEntry<>(source, 0.0)); // Insert source node with priority 0

        while (!heap.isEmpty()) {
            Map.Entry<GraphNode, Double> entry = heap.extractMin();
            GraphNode current = entry.getKey();
            double currentDistance = entry.getValue();

            if (current.equals(destination)) {
                break;
            }

            // Consider both incoming and outgoing edges for the current node
            for (Edge edge : edges) {
                if (edge.getSrc().equals(current.getLocationName()) || edge.getDest().equals(current.getLocationName())) {
                    GraphNode neighbor = edge.getSrc().equals(current.getLocationName()) ? getNodeByName(edge.getDest()) : getNodeByName(edge.getSrc());
                    double newDistance = currentDistance + edge.getDistance();
                    if (newDistance < distances.get(neighbor)) {
                        distances.put(neighbor, newDistance);
                        previous.put(neighbor, current);
                        heap.insert(new AbstractMap.SimpleEntry<>(neighbor, newDistance)); // Update priority in the heap
                    }
                }
            }
        }

        // Reconstruct the shortest path
        List<GraphNode> path = new ArrayList<>();
        GraphNode current = destination;
        while (current != null) {
            path.add(current);
            current = previous.get(current);
        }
        Collections.reverse(path);

        return path;
    }

    // Helper method to get DataStructures.GraphNode by its name
    private GraphNode getNodeByName(String name) {
        for (GraphNode node : nodes) {
            if (node.getLocationName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    public Set<String> getBlueNodes() {
        return blueNodes;
    }

    /**
     * Method that takes two locations, and a path to a topology, and calculates the distance between the two paths.
     * @param node1 The first location.
     * @param node2 The second location.
     * @param path Path to the topology file
     * @return The distance between the two nodes in the topology file.
     * @throws IOException
     */
    private static int distanceFromTopology(String node1, String node2, String path) throws IOException {
        String routeName = node1 + "To" + node2;
        List<String[]> topology = FileUtils.readFile(path);
        String[] route = null;
        double sum = 0;

        // Find the routing information.
        for (String[] strings: topology){
            String routeFromFile = strings[0].split("\\|")[0];
            if (Objects.equals(routeFromFile, routeName)){
                route = strings;
            }
        }

        for (String s: route){
            String[] arr = s.split("\\|");
            sum += Double.parseDouble(arr[arr.length - 1]);
        }

        return (int) sum;
    }



    public static void main(String[] args) throws IOException {
        Graph graph = new Graph("topology.txt");
        NetworkUI ui = new NetworkUI(graph);
        ui.setVisible(true);
        System.out.println(graph.getEdges());

        GraphNode sourceNode = graph.getNodeByName("FRESNO");
        GraphNode destinationNode = graph.getNodeByName("MINNEAPOLIS");

        long startTime = System.nanoTime(); // Record start time
        List<GraphNode> shortestPath = graph.shortestPath(sourceNode, destinationNode);
        long endTime = System.nanoTime(); // Record end time

        // Calculate the execution time
        long duration = (endTime - startTime);  // in nanoseconds

        // Convert nanoseconds to milliseconds for better readability
        double milliseconds = duration / 1e6;

        int hardwiredDistance = distanceFromTopology("nyc", "Fresno", "topology.txt");
        System.out.println("\n===Distance from " + sourceNode.getLocationName() + " to " + destinationNode.getLocationName() + "===");

        System.out.println("Shortest path found in " + milliseconds + " milliseconds.");
        int sumDistance = 0;
        System.out.print("Shortest Path: ");
        for (int i = 0; i < shortestPath.size(); i++) {
            GraphNode node = shortestPath.get(i);

            System.out.print(node.getLocationName());
            if (i < shortestPath.size() - 1) {
                // print the distance to the next node
                for (Edge edge : graph.getEdges()) {
                    if ((edge.getSrc().equals(node.getLocationName()) && edge.getDest().equals(shortestPath.get(i + 1).getLocationName())) ||
                            (edge.getDest().equals(node.getLocationName()) && edge.getSrc().equals(shortestPath.get(i + 1).getLocationName()))) {
                        System.out.print(" (" + edge.getDistance() + "km) -> ");
                        sumDistance += edge.getDistance();
                        break;
                    }
                }
            }
        }

        System.out.println("\n\nHardwired Path distance: " + hardwiredDistance + "km");
        System.out.println("Shortest possible path through network: " + sumDistance + "km");
        System.out.println("Difference in distance: " + (hardwiredDistance - sumDistance) + "km");
    }
}
