package com.bu.dong.fuseki.juc;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

public class ThreadPoolDemo {
    // 创建线程工厂（设置线程名称格式）
    private static final ThreadFactory namedThreadFactory =
            new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();

    // 创建线程池（完整参数配置）
    private static final ExecutorService threadPool = new ThreadPoolExecutor(
            12,          // 核心线程数
            900,                     // 最大线程数
            60L,                    // 空闲线程存活时间
            TimeUnit.SECONDS,       // 时间单位
            new ArrayBlockingQueue<>(100), // 任务队列（容量10）
            namedThreadFactory,     // 线程工厂
            new ThreadPoolExecutor.AbortPolicy() // 拒绝策略
    );

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        // 提交20个任务（测试队列满和拒绝策略）
        for (int i = 0; i < 1000; i++) {
            final int taskId = i;
            try {
                threadPool.execute(() -> {
                    try {
                        System.out.printf("[%s] Task-%d start%n", Thread.currentThread().getName(), taskId);
                        // 模拟任务执行
                        Thread.sleep(1000);
                        System.out.printf("[%s] Task-%d end%n", Thread.currentThread().getName(), taskId);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.printf("[%s] Thread interrupt", Thread.currentThread().getName());
                    }
                });
            } catch (RejectedExecutionException e) {
                // 拒绝新任务触发异常
                System.err.println("Task-" + taskId + " rejected: " + e.getMessage());
            }
        }

        // 1. 启动平缓关闭，表示不再接收新任务
        threadPool.shutdown();
        try {
            // 2. 等待线程池终止（最多60秒）
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                // 3. 超时后强制终止
                threadPool.shutdownNow();
                System.out.printf("[%s] ThreadPool be forced shutdown", Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            // 4. 处理等待期间的中断异常
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
            System.err.printf("[%s] Thread interrupt", Thread.currentThread().getName());
        }
        long totalCost = System.currentTimeMillis() - startTime;
        System.out.println("Total execution time: " + totalCost + "ms");
    }
}