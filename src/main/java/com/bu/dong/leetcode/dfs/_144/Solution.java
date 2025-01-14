package com.bu.dong.leetcode.dfs._144;

import com.bu.dong.leetcode.bfs.TreeNode;

import java.util.LinkedList;
import java.util.List;

public class Solution {
    // 存放前序遍历结果
    List<Integer> res = new LinkedList<>();

    // 返回前序遍历结果
    public List<Integer> preorderTraversal(TreeNode root) {
        traverse(root);
        return res;
    }

    // 二叉树遍历函数
    void traverse(TreeNode root) {
        if (root == null) {
            return;
        }
        // 前序位置
        res.add(root.val);
        traverse(root.left);
        traverse(root.right);
    }
}
