package IPSwitch;

import DataStructures.Node;

import java.util.*;
import java.util.stream.Collectors;

public class Paths {

    private final Map<String, Integer> cityToIndexMap;
    private final Map<Integer, Node> indexToNodeMap;
    private final Map<String, Node> nodesByCityName;
    private final Map<Node, Map<Node, Double>> adjacencyMatrixMap;

    public Paths(Map<String, Integer> cityToIndexMap, Map<String, Node> nodesByCityName, Map<Node, Map<Node, Double>> adjacencyMatrixMap) {
        this.cityToIndexMap = cityToIndexMap;
        this.nodesByCityName = nodesByCityName;
        this.adjacencyMatrixMap = adjacencyMatrixMap;
        this.indexToNodeMap = cityToIndexMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, e -> nodesByCityName.get(e.getKey())));
    }

    public Node getNodeByName(String cityName) {
        return nodesByCityName.get(cityName);
    }

    /**
     * Method to find the shortest path between two cities using Dijkstra's algorithm
     *
     * @param adjacencyMatrixMap The adjacency matrix of the graph
     * @param sourceCity         The name of the source city
     * @param destinationCity    The name of the destination city
     * @return a list of nodes representing the shortest path between the two cities
     */
    public List<Node> dijkstra(Map<Node, Map<Node, Double>> adjacencyMatrixMap, String sourceCity, String destinationCity) {
        double[][] adjacencyMatrix = convertAdjacencyMatrix(adjacencyMatrixMap);

        int source = cityToIndexMap.get(sourceCity);
        int destination = cityToIndexMap.get(destinationCity);

        int n = adjacencyMatrix.length;
        double[] distances = new double[n];
        double[] edgeDistances = new double[n];
        int[] previous = new int[n];
        boolean[] visited = new boolean[n];

        Arrays.fill(distances, Double.MAX_VALUE);
        Arrays.fill(edgeDistances, Double.MAX_VALUE);
        distances[source] = 0;
        edgeDistances[source] = 0;
        Arrays.fill(previous, -1);

        for (int i = 0; i < n; i++) {
            int node = -1;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && (node == -1 || distances[j] < distances[node]) && indexToNodeMap.get(j).isActive()) {
                    node = j;
                }
            }

            if (node == -1) {
                break;
            }

            if (node == destination) {
                break;
            }

            visited[node] = true;

            for (int j = 0; j < n; j++) {
                double weight = adjacencyMatrix[node][j];
                if (weight != 0 && distances[node] + weight < distances[j] && indexToNodeMap.get(j).isActive()) {
                    distances[j] = distances[node] + weight;
                    edgeDistances[j] = weight;
                    previous[j] = node;
                }
            }
        }

        List<Node> path = new ArrayList<>();
        List<String> pathWithDistances = new ArrayList<>();
        for (int at = destination; at != -1; at = previous[at]) {
            path.add(indexToNodeMap.get(at));
            pathWithDistances.add(indexToNodeMap.get(at).getName() + " (distance to next node: " + edgeDistances[at] + ")");
        }
        Collections.reverse(path); // reverse the path to get the correct order
        Collections.reverse(pathWithDistances);

        System.out.println("Shortest path(km) : " + String.join(" -> ", pathWithDistances));
        System.out.println("Total distance: " + distances[destination]);

        return path;
    }

    private double[][] convertAdjacencyMatrix(Map<Node, Map<Node, Double>> adjacencyMatrixMap) {
        int size = adjacencyMatrixMap.size();
        double[][] adjacencyMatrix = new double[size][size];

        for (int i = 0; i < size; i++) {
            Node node1 = indexToNodeMap.get(i);
            Map<Node, Double> node1Edges = adjacencyMatrixMap.get(node1);
            // node may still exist in the graph but unconnected
            if (node1Edges == null) {
                continue;
            }
            for (int j = 0; j < size; j++) {
                Node node2 = indexToNodeMap.get(j);
                adjacencyMatrix[i][j] = node1Edges.getOrDefault(node2, 0.0);
            }
        }

        return adjacencyMatrix;
    }

}