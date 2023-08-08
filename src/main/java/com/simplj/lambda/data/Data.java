package com.simplj.lambda.data;

class Data<T> {
    private Node<T> head;
    private Node<T> last;

    Data() {

    }
    Data(T val) {
        head = new Node<>(val);
        last = head;
    }

    Node<T> head() {
        return head;
    }

    void add(T val) {
        if (head == null) {
            head = new Node<>(val);
            last = head;
        } else {
            last = linkAfter(last, val);
        }
    }

    Node<T> linkAfter(Node<T> node, T val) {
        node.next = new Node<>(val, node.next);
        return node.next;
    }

    void removeHead() {
        head = head.next;
    }
    void removeNext(Node<T> n) {
        n.next = n.next.next;
    }

    static class Node<A> {
        private final A val;
        private Node<A> next;

        Node(A v) {
            this.val = v;
        }
        Node(A v, Node<A> n) {
            this.val = v;
            this.next = n;
        }

        A val() {
            return val;
        }

        Node<A> next() {
            return next;
        }
    }
}
