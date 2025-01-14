package com.bu.dong.leetcode.dfs._662;

import com.bu.dong.leetcode.bfs.TreeNode;

import java.util.HashMap;
import java.util.Map;

public class Solution {
    int ans = 0;
    Map<Integer, Integer> map = new HashMap<>();

    public int widthOfBinaryTree(TreeNode root) {
        dfs(root, 1, 0);
        return ans;
    }

    private void dfs(TreeNode node, int nodeIndex, int depth) {
        if (node == null) {
            return;
        }
        map.putIfAbsent(depth, nodeIndex);
        ans = Math.max(ans, nodeIndex - map.get(depth) + 1);
        dfs(node.left, 2 * nodeIndex, depth + 1);
        dfs(node.right, 2 * nodeIndex + 1, depth + 1);
    }
}