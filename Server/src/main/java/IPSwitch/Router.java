package IPSwitch;

import DataStructures.Node;
import DataStructures.NodeType;

import java.util.List;

/**
 * IPSwitch.Router class inherits from DataStructures.Node class and represents a router in the network.
 */

public class Router extends Node {
    private final Coordinates coordinates;

    /**
     * Constructor for the router.
     *
     * @param id   The id of the router.
     * @param name The name of the router.
     */

    public Router(int id, String name, Coordinates coordinates) {
        super(id, name, NodeType.ROUTER);
        this.coordinates = coordinates;
    }

    @Override
    public Coordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "ROUTER: " +  getName() + String.format(", Coordinates: %s", coordinates);
    }






   /*// router should have a capacity of 5 packets. If the queue is full, the packet should be dequeued and enqueued to the switch.
    public void enqueue (IPSwitch.Packet packet, IPSwitch.Switch source) {
        if (this.getQueue().size() > 5) {
            source.enqueue(packet);
            // dequeue two
        } else {
            this.enqueue(packet);
        }
    }*/
    }
