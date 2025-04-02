package com.bu.dong.fuseki.juc;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

// cas实现复杂，会存在aba问题，可通过版本号的方式解决
public class ConcurrentDoublyLinkedList<T> {
    private static class Node<T> {
        T val;
        AtomicReference<Node<T>> prev = new AtomicReference<>();
        AtomicReference<Node<T>> next = new AtomicReference<>();

        public Node(T data) {
            this.val = data;
        }
    }

    private final AtomicReference<Node<T>> head = new AtomicReference<>();
    private final AtomicReference<Node<T>> tail = new AtomicReference<>();
    private final AtomicInteger length = new AtomicInteger(0);

    // 插入头部
    public void addFirst(T data) {
        Node<T> newNode = new Node<>(data);
        Node<T> currentHead;
        do {
            currentHead = head.get();
            newNode.next.set(currentHead);
            if (currentHead != null) {
                currentHead.prev.set(newNode);
            }
        } while (!head.compareAndSet(currentHead, newNode));
        length.incrementAndGet();
    }

    // 插入尾部
    public void addLast(T data) {
        Node<T> newNode = new Node<>(data);
        Node<T> currentHead;
        do {
            currentHead = tail.get();
            newNode.next.set(currentHead);
            if (currentHead != null) {
                currentHead.next.set(newNode);
            }
        } while (!tail.compareAndSet(currentHead, newNode));
        length.incrementAndGet();
    }

    // 插入指定位置
    public void add(int index, T data) {
        if (index < 0) throw new IndexOutOfBoundsException();
        Node<T> newNode = new Node<>(data);
        while (true) {
            // 动态计算链表长度
            int size = calculateSize();
            if (index > size) throw new IndexOutOfBoundsException();

            if (index == 0) {
                addFirst(data);
                return;
            } else if (index == size) {
                addLast(data);
                return;
            } else {
                // 定位前驱节点和后继节点
                Node<T> predecessor = findPredecessor(index);
                Node<T> successor = predecessor.next.get();
                // 设置新节点指针
                newNode.prev.set(predecessor);
                newNode.next.set(successor);
                // 原子更新前驱的next和后继的prev
                if (predecessor.next.compareAndSet(successor, newNode)) {
                    if (successor.prev.compareAndSet(predecessor, newNode)) {
                        return;
                    } else {
                        // 回滚前驱的next指针
                        predecessor.next.compareAndSet(newNode, successor);
                    }
                }
            }
        }
    }

    // 动态计算链表长度（原子遍历）
    private int calculateSize() {
        int size = 0;
        Node<T> current = head.get();
        while (current != null) {
            current = current.next.get();
            size++;
        }
        return size;
    }

    // 定位前驱节点（原子遍历）
    private Node<T> findPredecessor(int index) {
        Node<T> current = head.get();
        for (int i = 0; i < index - 1; i++) {
            current = current.next.get();
        }
        return current;
    }


}
