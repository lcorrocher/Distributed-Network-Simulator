package DataStructures;

import java.util.NoSuchElementException;

/**
 * Generic representing a queue in the network.
 * Each queue has a front and rear node, and a size.
 */
public class Queue<T> {
    private Node<T> front;
    private Node<T> rear;
    private int size;

    /**
     * Inner class representing a node in the queue.
     * @param <T> The type of the data in the node. In our implementation it is a IPSwitch.Packet.
     */
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    /**
     * Constructor for the queue.
     */
    public Queue() {
        front = null;
        rear = null;
        size = 0;
    }

    /**
     * Method to add an item to the queue.
     * @param item The item to add to the queue.
     */
    public void enqueue(T item) {
        Node<T> newNode = new Node<>(item);
        if (isEmpty()) {
            front = newNode;
        } else {
            rear.next = newNode;
        }
        rear = newNode;
        size++;
    }

    /**
     * Method to remove an item from the queue.
     * @return The item removed from the queue.
     */
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("DataStructures.Queue is empty");
        }
        T data = front.data;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        size--;
        return data;
    }

    /**
     * Method to get the item at the front of the queue.
     * @return The item at the front of the queue.
     */
    public T top() {
        if (isEmpty()) {
            throw new NoSuchElementException("DataStructures.Queue is empty");
        }
        return front.data;
    }

    /**
     * Method to get the item at the rear of the queue.
     * @return The item at the rear of the queue.
     */
    public T peek(int n) {
        if (isEmpty() || n >= size || n < 0) {
            throw new NoSuchElementException("Invalid index or DataStructures.Queue is empty");
        }
        Node<T> current = front;
        for (int i = 0; i < n; i++) {
            current = current.next;
        }
        return current.data;
    }

    /**
     * Method to set the item to the object at the given index.
     * @param n The index to set the object at.
     * @param obj The object to set at the index. obj is a DataStructures.Queue for our implementation.
     */
    public void set(int n, T obj) {
        if (isEmpty() || n >= size || n < 0) {
            throw new NoSuchElementException("Invalid index or DataStructures.Queue is empty");
        }
        Node<T> current = front;
        for (int i = 0; i < n; i++) {
            current = current.next;
        }
        current.data = obj;
    }



    /**
     * Method to check if the queue is empty.
     * @return True if the queue is empty, false otherwise.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Method to get the size of the queue.
     * @return The size of the queue.
     */
    public int size() {
        return size;
    }

    /**
     * Method to get the string representation of the queue.
     * @return The string representation of the queue.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("]");
        Node<T> current = front;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(",");
            }
            current = current.next;
        }
        sb.append("[");
        return sb.reverse().toString();
    }
}

