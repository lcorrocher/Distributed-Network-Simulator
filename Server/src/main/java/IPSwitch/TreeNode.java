package IPSwitch;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in a tree structure, specifically for switches.
 */
class TreeNode {
    private final Switch switchNode;
    private final List<TreeNode> children;

    /**
     * Constructs a IPSwitch.TreeNode with the given switchNode and initializes an empty list of children.
     * @param switchNode the switch node associated with this tree node
     */
    public TreeNode(Switch switchNode) {
        this.switchNode = switchNode;
        this.children = new ArrayList<>();
    }

    /**
     * Retrieves the switch node associated with this tree node.
     * @return the switch node
     */
    public Switch getSwitchNode() {
        return switchNode;
    }

    /**
     * Retrieves the list of children nodes of this tree node.
     * @return the list of children nodes
     */
    public List<TreeNode> getChildren() {
        return children;
    }

    /**
     * Adds a child node to this tree node.
     * @param child the child node to add
     */
    public void addChild(TreeNode child) {
        children.add(child);
    }
}