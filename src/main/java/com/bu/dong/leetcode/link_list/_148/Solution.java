package com.bu.dong.leetcode.link_list._148;

import com.bu.dong.leetcode.link_list.ListNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Solution {
    public ListNode sortList(ListNode head) {
        List<Integer> num = new ArrayList<>();
        ListNode current = head;
        while (current != null) {
            num.add(current.val);
            current = current.next;
        }
        Collections.sort(num);
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;
        for (int n : num) {
            tail.next = new ListNode(n);
            tail = tail.next;
        }
        return dummy.next;
    }

    // 归并排序
    public ListNode sortList_1(ListNode head) {
        if (head == null || head.next == null)
            return head;
        ListNode fast = head.next, slow = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        ListNode tmp = slow.next;
        slow.next = null;
        ListNode left = sortList_1(head);
        ListNode right = sortList_1(tmp);
        ListNode h = new ListNode(0);
        ListNode res = h;
        while (left != null && right != null) {
            if (left.val < right.val) {
                h.next = left;
                left = left.next;
            } else {
                h.next = right;
                right = right.next;
            }
            h = h.next;
        }
        h.next = left != null ? left : right;
        return res.next;
    }
}
