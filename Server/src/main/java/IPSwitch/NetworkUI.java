package IPSwitch;

import DataStructures.Edge;
import DataStructures.Graph;
import DataStructures.GraphNode;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Boilerplate SwingUI to display our network.
 */
public class NetworkUI extends JFrame {
    private final Graph graph;
    private final Map<String, Point> nodePositions;


    public NetworkUI(Graph graph) {
        this.graph = graph;
        nodePositions = new HashMap<>();
        initUI();
        generateNodePositions();
    }

    private void initUI() {
        setTitle("DataStructures.Graph Visualization of WAN IPSwitch.Network. Blue are source and destination cities");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void generateNodePositions() {
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = 200;

        // this manually set x and y into a septagon
        for (int i = 0; i < graph.getNodes().size(); i++) {
            double angle = 2 * Math.PI / 7 * i;
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));
            GraphNode node = graph.getNodes().get(i);
            nodePositions.put(node.getLocationName(), new Point(x, y));
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        drawEdges(g);
        drawNodesAndLabels(g, graph.getBlueNodes());
    }

    private void drawEdges(Graphics g) {
        for (Edge edge : graph.getEdges()) {
            Point srcPoint = nodePositions.get(edge.getSrc());
            Point destPoint = nodePositions.get(edge.getDest());
            if (srcPoint != null && destPoint != null) {
                g.drawLine(srcPoint.x, srcPoint.y, destPoint.x, destPoint.y);
                int midX = (srcPoint.x + destPoint.x) / 2;
                int midY = (srcPoint.y + destPoint.y) / 2;
                g.drawString(edge.getDistance() + "km", midX, midY);
            }
        }
    }


    private void drawNodesAndLabels(Graphics g, Set<String> blueNodes) {

        for (String nodeName : nodePositions.keySet()) {
            Point point = nodePositions.get(nodeName);
            int nodeDiameter = 40;
            Color nodeColor = blueNodes.contains(nodeName.toUpperCase()) ? Color.BLUE : Color.BLACK;

            g.setColor(nodeColor);
            g.fillOval(point.x - nodeDiameter / 2, point.y - nodeDiameter / 2, nodeDiameter, nodeDiameter);
            FontMetrics metrics = g.getFontMetrics();
            int nameWidth = metrics.stringWidth(nodeName);
            g.drawString(nodeName, point.x - nameWidth / 2, point.y - nodeDiameter / 2 - 5);
        }
    }

}