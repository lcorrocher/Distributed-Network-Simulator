package DataStructures;

import java.util.Iterator;

public class LinkedList<E> implements Iterable<E> {

    private static class Node<E> {
        private E element;
        private Node<E> next;
        public Node(E e, Node<E> n) {
            this.element = e;
            this.next = n;
        }
        public E getElement() {
            return element;
        }
        public Node<E> next() {
            return next;
        }
        public void setNext(Node<E> n) {
            this.next = n;
        }


    }


    private Node<E> head = null;
    private Node<E> tail = null;
    private int size = 0;
    public LinkedList() {
        head = null;
        size = 0;
    };
    public int getSize() {return size;}
    public boolean isEmpty() {return size == 0;}
    public E first() {
        if (isEmpty()) {
            return null;
        }
        return head.getElement();
    }
    public E last() {
        if (isEmpty()) {
            return null;
        }
        return tail.getElement();
    }

    public void addFirst(E e) {
        head = new Node<>(e, head);
        if (size == 0) {
            tail = head;
        }
        size++;
    }

    public void add(E e) {
        Node<E> newest = new Node<>(e, null);
        if (isEmpty()) {
            head = newest;
        } else {
            tail.setNext(newest);
        }
        tail = newest;
        size++;
    }

    public E removeFirst() {
        if (isEmpty()) {
            return null;
        }
        E element = head.getElement();
        head = head.next();
        size--;
        if (size == 0) {
            tail = null;
        }
        return element;
    }

    @Override
    public Iterator<E> iterator() {
        return new LinkedListIterator();
    }

    private class LinkedListIterator implements Iterator<E> {
        private Node<E> current = head;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            E element = current.getElement();
            current = current.next();
            return element;
        }
    }

}
