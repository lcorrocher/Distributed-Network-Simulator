package IPSwitch;

import java.util.*;
import java.util.stream.Collectors;

public class Paths {

    private final Map<String, Integer> cityToIndexMap;
    private final Map<Integer, String> indexToCityMap;

    public Paths(Map<String, Integer> cityToIndexMap) {
        this.cityToIndexMap = cityToIndexMap;
        this.indexToCityMap = cityToIndexMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    /**
     * Method to find the shortest path between two cities using Dijkstra's algorithm
     *
     * @param adjacencyMatrix The adjacency matrix representing the graph
     * @param sourceCity      The source city
     * @param destinationCity The destination city
     * @return The shortest path between the two cities as a list of city names
     */
    public List<String> dijkstra(double[][] adjacencyMatrix, String sourceCity, String destinationCity) {
        int source = cityToIndexMap.get(sourceCity);
        int destination = cityToIndexMap.get(destinationCity);

        int n = adjacencyMatrix.length;
        double[] distances = new double[n];
        int[] previous = new int[n];
        boolean[] visited = new boolean[n];

        Arrays.fill(distances, Double.MAX_VALUE);
        distances[source] = 0;
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
                    previous[j] = node;
                }
            }
        }

        // Construct the shortest path
        List<String> path = new ArrayList<>();
        for (int at = destination; at != -1; at = previous[at]) {
            path.add(indexToCityMap.get(at));
        }
        Collections.reverse(path);

        System.out.println("Shortest path: " + path);
        System.out.println("Total distance: " + distances[destination]);

        return path;
    }
}