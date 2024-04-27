package IPSwitch;

import DataStructures.Node;
import utils.Colour;
import utils.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class Paths {

    private final Map<String, Integer> cityToIndexMap;
    private final Map<Integer, Node> indexToNodeMap;
    private final Map<String, Node> nodesByCityName;

    public Paths(Map<String, Integer> cityToIndexMap, Map<String, Node> nodesByCityName) {
        this.cityToIndexMap = cityToIndexMap;
        this.nodesByCityName = nodesByCityName;
        this.indexToNodeMap = cityToIndexMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, e -> nodesByCityName.get(e.getKey())));
    }

    private Node getNodeByName(String cityName) {
        return nodesByCityName.get(cityName);
    }

    /**
     * Method to find the shortest path between two cities using Dijkstra's algorithm
     * @param adjacencyMatrixMap The adjacency matrix of the graph
     * @param sourceCity The name of the source city
     * @param destinationCity The name of the destination city
     * @return a list of nodes representing the shortest path between the two cities
     */
    public List<Node> dijkstra(Map<Node, Map<Node, Double>> adjacencyMatrixMap, String sourceCity, String destinationCity) {
        long startTime = System.currentTimeMillis();
        double[][] adjacencyMatrix = convertAdjacencyMatrix(adjacencyMatrixMap);

        int source = cityToIndexMap.get(sourceCity);
        int destination = cityToIndexMap.get(destinationCity);

        System.out.println("Adjacency matrix:\n");
        for (double[] row : adjacencyMatrix) {
            System.out.println(Arrays.toString(row));
        }

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
                if (!visited[j] && (node == -1 || distances[j] < distances[node])) {
                    node = j;
                }
            }

            if (node == destination) {
                break;
            }

            visited[node] = true;

            for (int j = 0; j < n; j++) {
                double weight = adjacencyMatrix[node][j];
                if (weight != 0 && distances[node] + weight < distances[j]) {
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
        Collections.reverse(path);
        Collections.reverse(pathWithDistances);

        System.out.println("Shortest path(km) : " + String.join(" -> ", pathWithDistances));
        System.out.println("Total distance: " + distances[destination]);

        long timeTaken = System.currentTimeMillis() - startTime;
        Logger.log("[Dijkstra's algorithm] Time taken to execute: " + timeTaken );
        System.out.println(Colour.yellow("Time has been logged"));
        return path;
    }

    private double[][] convertAdjacencyMatrix(Map<Node, Map<Node, Double>> adjacencyMatrixMap) {
        int size = adjacencyMatrixMap.size();
        double[][] adjacencyMatrix = new double[size][size];

        for (int i = 0; i < size; i++) {
            Node node1 = indexToNodeMap.get(i);
            for (int j = 0; j < size; j++) {
                Node node2 = indexToNodeMap.get(j);
                adjacencyMatrix[i][j] = adjacencyMatrixMap.get(node1).getOrDefault(node2, 0.0);
            }
        }

        return adjacencyMatrix;
    }
}