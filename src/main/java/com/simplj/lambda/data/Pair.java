package com.simplj.lambda.data;

import com.simplj.lambda.tuples.Tuple2;

class Pair<T, R> {
    private Node<T, R> head;
    private Node<T, R> last;

    Pair() {

    }
    Pair(T key, R val) {
        head = new Node<>(key, val);
        last = head;
    }

    Node<T, R> head() {
        return head;
    }

    void add(T key, R val) {
        if (head == null) {
            head = new Node<>(key, val);
            last = head;
        } else {
            last = linkAfter(last, key, val);
        }
    }

    void add(Tuple2<T, R> t) {
        add(t.first(), t.second());
    }

    Node<T, R> linkAfter(Node<T, R> node, T key, R val) {
        node.next = new Node<>(key, val, node.next);
        return node.next;
    }

    void removeHead() {
        head = head.next;
    }
    void removeNext(Node<T, R> n) {
        n.next = n.next.next;
    }

    static class Node<A, B> {
        private final A key;
        private final B val;
        private Node<A, B> next;

        Node(A a, B b) {
            this.key = a;
            this.val = b;
        }
        Node(A a, B b, Node<A, B> n) {
            this.key = a;
            this.val = b;
            this.next = n;
        }

        A key() {
            return key;
        }
        B val() {
            return val;
        }

        Node<A, B> next() {
            return next;
        }
    }
}
