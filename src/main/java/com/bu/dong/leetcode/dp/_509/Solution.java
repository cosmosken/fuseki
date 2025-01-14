package com.bu.dong.leetcode.dp._509;

public class Solution {
    int fib(int N) {
        // 备忘录全初始化为 0
        int[] memo = new int[N + 1];
        // 进行带备忘录的递归
        return dp(memo, N);
    }

    // 备忘录版本
    int dp(int[] memo, int n) {
        // base case
        if (n == 0 || n == 1) return n;
        // 已经计算过，不用再计算了
        if (memo[n] != 0) return memo[n];
        memo[n] = dp(memo, n - 1) + dp(memo, n - 2);
        return memo[n];
    }
}
