package com.bu.dong.fuseki.juc;

class Node<T> {
    T val;
    Node<T> prev;
    Node<T> next;

    public Node(T data) {
        this.val = data;
        this.prev = null;
        this.next = null;
    }
}