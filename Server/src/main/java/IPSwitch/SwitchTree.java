package IPSwitch;

/**
 * A tree structure representing a hierarchy of switches.
 */
class SwitchTree {
    private TreeNode root;

    /**
     * Constructs a IPSwitch.SwitchTree with the given switchNode as the root node.
     * @param switchNode the root switch node of the tree
     */
    public SwitchTree(Switch switchNode) {
        this.root = new TreeNode(switchNode);
    }

    /**
     * Retrieves the root node of the tree.
     * @return the root node of the tree
     */
    public TreeNode getRoot() {
        return root;
    }

    /**
     * Generates a string representation of the tree, traversed in a hierarchical manner.
     * @return a string representing the tree
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        traverseTree(root, sb, 0);
        return sb.toString();
    }

    /**
     * Traverses the tree recursively to build a hierarchical string representation.
     * @param node the current node being traversed
     * @param sb the StringBuilder to append the node information
     * @param depth the current depth in the tree, used for indentation
     */
    private void traverseTree(TreeNode node, StringBuilder sb, int depth) {
        if (node == null) return;

        // Append current node's switch information
        for (int i = 0; i < depth; i++) {
            sb.append("  "); // Indentation for hierarchy
        }
        sb.append(node.getSwitchNode().getName()).append(" (ID: ").append(node.getSwitchNode().getId()).append(")\n");

        // Traverse children recursively
        for (TreeNode child : node.getChildren()) {
            traverseTree(child, sb, depth + 1);
        }
    }

    /**
     * Finds a switch node in the tree by its name.
     * @param name the name of the switch node to find
     * @return the IPSwitch.TreeNode representing the switch node if found, otherwise null
     */
    public TreeNode findSwitchNodeByName(String name) {
        return findSwitchNodeByName(root, name);
    }

    /**
     * Our Depth First Search algorithm. Recursively searches for a switch node with the given name starting from the specified node.
     * @param node the current node being examined
     * @param name the name of the switch node to find
     * @return the IPSwitch.TreeNode representing the switch node if found, otherwise null
     */
    private TreeNode findSwitchNodeByName(TreeNode node, String name) {
        if (node == null || node.getSwitchNode().getName().equals(name)) {
            return node;
        }

        // Recursively search in the children nodes
        for (TreeNode child : node.getChildren()) {
            TreeNode result = findSwitchNodeByName(child, name);
            if (result != null) {
                return result;
            }
        }

        // If not found, return null
        return null;
    }
}