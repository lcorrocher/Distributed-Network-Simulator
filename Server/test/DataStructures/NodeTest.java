package DataStructures;

import IPSwitch.Coordinates;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeTest {

    /**
     * Stub implementation of Node for testing purposes.e.g. Router, Switch
     */
    private static class NodeStub extends Node {
        private final Coordinates coordinates;

        public NodeStub(int id, String name, NodeType type, Coordinates coordinates) {
            super(id, name, type);
            this.coordinates = coordinates;
        }

        @Override
        public Coordinates getCoordinates() {
            return coordinates;
        }
    }

    @Test
    public void testDistanceTo() {
        // Create mock objects for dublin and london
        Coordinates dublinCoords = new Coordinates(53.349805, -6.26031);
        Coordinates londonCoords = new Coordinates(51.507351, -0.127758);
        Node dublin = new NodeStub(1, "Dublin", NodeType.ROUTER, dublinCoords);
        Node london = new NodeStub(2, "London", NodeType.ROUTER, londonCoords);

        double expectedDistance = 464.2;

        double actualDistance = dublin.getDistanceTo(london);

        System.out.println("Actual distance: " + actualDistance);

        assertEquals(expectedDistance, actualDistance, 1); // Tolerance 1 km
    }
}
