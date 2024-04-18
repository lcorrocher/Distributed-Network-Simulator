package DataStructures;

import java.util.Map;

/**
 * Class representation of the DataStructures.Heap data structure.
 */
public class Heap {
    private Map.Entry<GraphNode, Double>[] heap;
    private int size;
    private int capacity;

    /**
     * Constructor for our heap.
     * @param capacity Size of the heap.
     */
    public Heap(int capacity) {
        this.capacity = capacity;
        heap = new Map.Entry[capacity];
        size = 0;
    }

    /**
     * Calculates index of the parent node.
     * @param index Index of current node.
     * @return Index of the parent node.
     */
    private int parent(int index) {
        return (index - 1) / 2;
    }

    /**
     * Calculates the index of the left node.
     * @param index Index of the current node.
     * @return Index of the left child node.
     */
    private int leftChild(int index) {
        return 2 * index + 1;
    }

    /**
     * Calculates the index of the right child node.
     * @param index Index of the current node.
     * @return Index of the right child node.
     */
    private int rightChild(int index) {
        return 2 * index + 2;
    }

    /**
     * Swaps the elements at two indices in the heap.
     * @param index1 Index of the first element.
     * @param index2 Index of the second element.
     */
    private void swap(int index1, int index2) {
        Map.Entry<GraphNode, Double> temp = heap[index1];
        heap[index1] = heap[index2];
        heap[index2] = temp;
    }

    /**
     * Inserts a new value into the heap.
     * @param value The value we want stored in the heap.
     */
    public void insert(Map.Entry<GraphNode, Double> value) {
        if (size == capacity) {
            System.out.println("DataStructures.Heap is full");
            return;
        }
        heap[size] = value;
        int currentIndex = size;
        size++;
        while (currentIndex > 0 && heap[currentIndex].getValue() < heap[parent(currentIndex)].getValue()) {
            swap(currentIndex, parent(currentIndex));
            currentIndex = parent(currentIndex);
        }
    }

    /**
     * @return The minimum value in the heap.
     */
    public Map.Entry<GraphNode, Double> extractMin() {
        if (size == 0) {
            System.out.println("DataStructures.Heap is empty");
            return null;
        }
        Map.Entry<GraphNode, Double> min = heap[0];
        heap[0] = heap[size - 1];
        size--;
        heapify(0);
        return min;
    }

    /**
     * Restores the heap property at a given index.
     * @param index The index to start the heapify process at.
     */
    private void heapify(int index) {
        int smallest = index;
        int left = leftChild(index);
        int right = rightChild(index);
        if (left < size && heap[left].getValue() < heap[smallest].getValue()) {
            smallest = left;
        }
        if (right < size && heap[right].getValue() < heap[smallest].getValue()) {
            smallest = right;
        }
        if (smallest != index) {
            swap(index, smallest);
            heapify(smallest);
        }
    }

    /**
     * Prints out the heap.
     */
    public void printHeap() {
        for (int i = 0; i < size; i++) {
            System.out.print(heap[i].getKey().getLocationName() + ":" + heap[i].getValue() + " ");
        }
        System.out.println();
    }

    /**
     * Checks if the heap is empty.
     * @return True if heap is empty.
     */
    public boolean isEmpty() {
        return size == 0;
    }
}
