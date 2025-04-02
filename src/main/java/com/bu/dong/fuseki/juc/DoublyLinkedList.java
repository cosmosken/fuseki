package com.bu.dong.fuseki.juc;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DoublyLinkedList<T> {
    private Node<T> head;
    private Node<T> tail;
    private int length = 0;  // 初始化长度
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // 插入尾部
    public void addLast(T data) {
        lock.writeLock().lock();
        try {
            Node<T> newNode = new Node<>(data);
            // 更新尾节点
            if (tail == null) {  // 空链表
                head = newNode;
            } else {
                newNode.prev = tail;
                tail.next = newNode;
            }
            tail = newNode;
            this.length++;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 插入头部
    public void addFirst(T data) {
        lock.writeLock().lock();
        try {
            Node<T> newNode = new Node<>(data);
            if (head == null) {  // 空链表
                head = newNode;
                tail = newNode;
            } else {
                newNode.next = head;
                head.prev = newNode;
                head = newNode;  // 更新头节点
            }
            this.length++;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 插入指定位置
    public void add(int index, T data) {
        if (index < 0 || index > this.length) throw new IndexOutOfBoundsException();
        if (index == 0) {
            addFirst(data);
        } else if (index == this.length) {
            addLast(data);
        } else {
            Node<T> current = getNode(index);  // 获取目标位置节点
            Node<T> newNode = new Node<>(data);
            // 调整指针
            newNode.prev = current.prev;
            newNode.next = current;
            current.prev.next = newNode;
            current.prev = newNode;
            this.length++;
        }
    }

    // 获取索引位置的节点（可优化为从头部或尾部遍历）
    private Node<T> getNode(int index) {
        lock.readLock().lock();
        try {
            Node<T> current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            return current;
        } finally {
            lock.readLock().unlock();
        }
    }

}
