package IPSwitch;

import DataStructures.Node;
import DataStructures.NodeType;


/**
 * Class representing a switch in the network, which inherits from the DataStructures.Node class.
 * Each switch stores packets in a stack, and has a folder path to store the packets.
 * At the end of the network, the packets will be sent to the destination switch, before being sent to the destination folder.
 */

public class Switch extends Node {
    private final String folderPath;
    private SwitchTree lanTree;

    public Switch(int id, String name, String folderPath) {
        super(id, name, NodeType.SWITCH);
        this.folderPath = folderPath;
        this.lanTree = new SwitchTree(this);
    }

    /**
     * Method to send packets to the destination switch.
     * @return The packets sent to the destination switch.
     */
    public String getFolderPath() {
        return folderPath;
    }

    /**
     * Method to return our current tree
     * @return The lan tree is returned.
     */
    public SwitchTree getLanTree() {
        return lanTree;
    }
}