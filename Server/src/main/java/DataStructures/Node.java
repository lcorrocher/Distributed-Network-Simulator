package DataStructures;

import IPSwitch.Coordinates;
import IPSwitch.Packet;

import java.util.NoSuchElementException;


/**
 * Custom node class to represent a node in the network
 * DataStructures.Node takes an enum DataStructures.NodeType to determine the type of node
 * DataStructures.NodeType is either a subclass SWITCH or ROUTER
 * Each node type has a queue of packets and different methods to manipulate the queue
 * Each node has a unique id and name. The id, name and type are set in the constructor and cannot be changed.
 */

public abstract class Node {

    private final int id;
    private final String name;
    private final NodeType type;
    private Queue<Packet> q;

    public Node(int id, String name, NodeType type) {
        this.id = id;
        this.name = name;
        this.type = type;

        q = new Queue<>();
    }

    public abstract Coordinates getCoordinates();

    public double getDistanceTo(Node otherNode) {
        return this.getCoordinates().haversine(otherNode.getCoordinates());
    }


    /**
     * Method to find the index of the first packet with the target message id, i.e. the top packet's msgId
     * @param targetMsgId The top packet's msgId
     * @return The indexes of the first two packets with the target message id in the queue
     */
    public int[] findIndexCommonMsgId(String targetMsgId) {

        int[] indexes = new int[2];
        int count = 0;
        int index = 0;

        // Traverse the queue and find the first two packets with the target message id
        while (count < 2) {
            if (q.peek(index).getMsgId().equals(targetMsgId)) {
                indexes[count++] = index;
            }
            index++;
        }
        return indexes;
    }


    /**
     * Method to swap two elements in the queue
     * @param q The queue to swap elements in
     * @param idx1 The index of the first element to swap
     * @param idx2 The index of the second element to swap
     */
    public void swap(Queue<Packet> q, int idx1, int idx2) {

        if (q.isEmpty() || idx1 < 0 || idx2 < 0 || idx1 > q.size() || idx2 > q.size()) {
            throw new NoSuchElementException("Invalid index or DataStructures.Queue is empty. Cannot swap");
        } else {
            Packet a = q.peek(idx1);
            Packet b = q.peek(idx2);
            // debug: System.out.println("Swapping " + a + " and " + b);

            q.set(idx1, b);
            q.set(idx2, a);
            // debug: System.out.println("swapped " + q.peek(idx1) + " and " + q.peek(idx2));
        }
    }


    public Queue<Packet> getQueue() {
        return this.q;
    }

    public NodeType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void enqueue(Packet packet){
        q.enqueue(packet);
    }

    public Packet dequeue() {
        return q.dequeue();
    }

    public Packet top(){
        return q.top();
    }

    public Packet peek(int n) {
        return q.peek(n);
    }

    @Override
    public String toString() {
        return String.format("Node %d: %s", id, name);
    }
}