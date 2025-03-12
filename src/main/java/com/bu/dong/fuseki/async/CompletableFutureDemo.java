package com.bu.dong.fuseki.async;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompletableFutureDemo {
    //    private static final Logger Log = Logger.getLogger(CompletableFutureDemo.class);
    private static final class Log {
        static void info(String msg) {
            System.out.println("[INFO] " + msg);
        }

        static void error(String msg) {
            System.err.println("[ERROR] " + msg);
        }
    }

    @Data
    static class Product {
        private String id;
        private String name;
        private BigDecimal price;
        private int stock;

        public Product(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return String.format("Product{id='%s', name='%s', price=%s, stock=%d}", id, name, price, stock);
        }
    }

    // 自定义线程池（修复3: 调整拒绝策略为记录日志）
    private static final ExecutorService executor = new ThreadPoolExecutor(
            2, 5, 30, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10),
            new ThreadFactory() {
                private int count = 0;

                @Override
                public Thread newThread(@NotNull Runnable r) {
                    return new Thread(r, "supplier-thread-" + count++);
                }
            },
            (r, pool) -> Log.error("任务被拒绝，当前活跃线程：" + pool.getActiveCount())
    );

    // 供应商接口
    interface SupplierService {
        CompletableFuture<Product> getProduct(String productId);
    }

    static class SupplierA implements SupplierService {
        @Override
        public CompletableFuture<Product> getProduct(String productId) {
            return CompletableFuture.supplyAsync(() -> {
                Log.info("SupplierA 开始处理");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) { /* 忽略中断 */ }
                Product p = new Product(productId, "ProductA");
                p.setPrice(new BigDecimal("100.00"));
                p.setStock(10);
                return p;
            }, executor);
        }
    }

    static class SupplierB implements SupplierService {
        @Override
        public CompletableFuture<Product> getProduct(String productId) {
            return CompletableFuture.supplyAsync(() -> {
                Log.info("SupplierB 开始处理");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) { /* 忽略中断 */ }
                Product p = new Product(productId, "ProductB");
                p.setPrice(new BigDecimal("99.00"));
                p.setStock(10);
                return p;
            }, executor);
        }
    }

    static class SupplierC implements SupplierService {
        @Override
        public CompletableFuture<Product> getProduct(String productId) {
            return CompletableFuture.supplyAsync(() -> {
                Log.info("SupplierC 开始处理");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) { /* 忽略中断 */ }
                Product p = new Product(productId, "ProductC");
                p.setPrice(new BigDecimal("98.00"));
                p.setStock(10);
                return p;
            }, executor);
        }
    }

    public static void main(String[] args) {
        SupplierService supplierA = new SupplierA();
        SupplierService supplierB = new SupplierB();
        SupplierService supplierC = new SupplierC();

        // 定义带超时和降级的任务
        CompletableFuture<Product> futureA = supplierA.getProduct("P1001")
                .orTimeout(1, TimeUnit.SECONDS)
                .exceptionally(ex -> handleError("SupplierA", "P1001"));
        CompletableFuture<Product> futureB = supplierB.getProduct("P1002")
                .orTimeout(1, TimeUnit.SECONDS)
                .exceptionally(ex -> handleError("SupplierB", "P1002"));
        CompletableFuture<Product> futureC = supplierC.getProduct("P1003")
                .orTimeout(2, TimeUnit.SECONDS)
                .exceptionally(ex -> handleError("SupplierC", "P1003"));

        // 非阻塞聚合结果
        CompletableFuture<List<Product>> aggregateFuture = CompletableFuture.allOf(futureA, futureB, futureC)
                .thenApply(v -> Stream.of(futureA, futureB, futureC)
                        .map(f -> f.handle((res, ex) -> ex != null ? createFallbackProduct(ex) : res))
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );

        try {
            List<Product> products = aggregateFuture.get(4, TimeUnit.SECONDS); // 全局超时略大于单任务总和
            Log.info("聚合结果: " + products);
        } catch (TimeoutException e) {
            Log.error("全局聚合超时");
        } catch (Exception e) {
            Log.error("任务失败: " + e.getMessage());
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    Log.error("线程池未完全关闭，强制终止");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static Product handleError(String supplier, String productId) {
        Log.error(supplier + " 任务失败，返回默认商品");
        return new Product(productId, "默认商品");
    }

    // 降级商品生成方法
    private static Product createFallbackProduct(Throwable ex) {
        Log.error("降级逻辑触发，异常原因：" + ex.getMessage());
        Product fallback = new Product("FALLBACK_001", "默认商品");
        fallback.setPrice(new BigDecimal("0.00"));
        fallback.setStock(0);
        return fallback;
    }
}
