package com.bu.dong.fuseki.juc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadLocalDemo {
    // 线程变量（每个线程独立存储）
    private static final ThreadLocal<SimpleDateFormat> dateFormatHolder =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    private static final ExecutorService pool = Executors.newFixedThreadPool(3);

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            pool.execute(() -> {
                try {
                    // 使用线程变量
                    SimpleDateFormat format = dateFormatHolder.get();
                    String date = format.format(new Date());
                    System.out.println(Thread.currentThread().getName()
                            + " formatted date: " + date);
                } finally {
                    // 必须清理线程变量（防止内存泄漏）
                    dateFormatHolder.remove();
                }
            });
        }
        pool.shutdown();
    }
}