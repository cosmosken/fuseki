package com.bu.dong.fuseki.async;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

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
        CompletableFuture<Product> featureA = supplierA.getProduct("P1001").orTimeout(1, TimeUnit.SECONDS);
        CompletableFuture<Product> featureB = supplierB.getProduct("P1002").orTimeout(1, TimeUnit.SECONDS);
        CompletableFuture<Product> featureC = supplierC.getProduct("P1003").orTimeout(1, TimeUnit.SECONDS);

        // 聚合任务到线程池
        CompletableFuture<List<Product>> aggregateFuture = CompletableFuture.allOf(featureA, featureB, featureC)
                .thenApply(v -> Arrays.asList(featureA.join(), featureB.join(), featureC.join()));
        try {
            List<Product> products = aggregateFuture.get(3, TimeUnit.SECONDS);
            Log.info("聚合结果: " + products);
        } catch (TimeoutException e) {
            Log.error("任务超时");
        } catch (Exception e) {
            Log.error("任务失败: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
