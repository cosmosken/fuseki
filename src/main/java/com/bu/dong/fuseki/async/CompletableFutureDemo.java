package com.bu.dong.fuseki.async;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletableFutureDemo {

    // 自定义日志工具
    private static class Log {
        static void info(String msg) {
            System.out.println("[INFO] " + msg);
        }

        static void error(String msg) {
            System.err.println("[ERROR] " + msg);
        }
    }

    // 商品实体类
    @Data
    static class Product {
        private String id;
        private String name;
        private BigDecimal price;
        private int stock;

        private Product(String id, String name) {
            this.id = id;
            this.name = name;
        }

        // 静态工厂方法
        public static Product create(String id, String name) {
            return new Product(id, name);
        }

        // 带详情的工厂方法
        public static Product withDetails(String id, String name, BigDecimal price, int stock) {
            Product p = new Product(id, name);
            p.setPrice(price);
            p.setStock(stock);
            return p;
        }

        // 降级商品工厂
        public static Product fallback(String id) {
            return new Product(id, "[降级商品]");
        }
    }

    // 线程池配置
    private static final ExecutorService asyncExecutor = new ThreadPoolExecutor(
            4, 8, 30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(0);

                @Override
                public Thread newThread(@NotNull Runnable r) {
                    return new Thread(r, "async-pool-" + counter.getAndIncrement());
                }
            },
            (r, executor) -> Log.error("线程池满载拒绝任务，活跃线程数：" + executor.getActiveCount())
    );

    // 异步任务执行器
    private static class AsyncTask {
        public static CompletableFuture<Product> fetchProduct(String productId, long timeoutMs) {
            return CompletableFuture.supplyAsync(() -> {
                        Log.info("开始查询商品: " + productId);
                        try {
                            Thread.sleep(300); // 模拟网络延迟
                            return Product.withDetails(productId, "iPhone15", new BigDecimal("6999.00"), 100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(e);
                        }
                    }, asyncExecutor)
                    .orTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                    .exceptionally(ex -> {
                        Log.error("商品查询失败: " + ex.getMessage());
                        return Product.fallback(productId);
                    });
        }

        public static CompletableFuture<Integer> fetchStock(String productId, long timeoutMs) {
            return CompletableFuture.supplyAsync(() -> {
                        Log.info("开始查询库存: " + productId);
                        try {
                            Thread.sleep(200); // 模拟数据库查询
                            return 100;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(e);
                        }
                    }, asyncExecutor)
                    .orTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                    .exceptionally(ex -> {
                        Log.error("库存查询失败: " + ex.getMessage());
                        return 0; // 降级为0库存
                    });
        }
    }

    public static void main(String[] args) {
        // 并行执行三个异步任务
        CompletableFuture<Product> productFuture = AsyncTask.fetchProduct("P1001", 500);
        CompletableFuture<Integer> stockFuture = AsyncTask.fetchStock("P1001", 300);
        CompletableFuture<BigDecimal> priceFuture = CompletableFuture.supplyAsync(() -> {
            Log.info("开始计算促销价");
            return new BigDecimal("6499.00");
        }, asyncExecutor);

        // 聚合结果
        CompletableFuture<Void> aggregateFuture = CompletableFuture.allOf(productFuture, stockFuture, priceFuture)
                .thenRunAsync(() -> {
                    Product product = productFuture.join();
                    int stock = stockFuture.join();
                    BigDecimal price = priceFuture.join();

                    Log.info(String.format("聚合结果: %s | 库存: %d | 促销价: %s",
                            product, stock, price));
                }, asyncExecutor);

        try {
            // 全局超时控制
            aggregateFuture.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Log.error("全局任务超时");
        } catch (Exception e) {
            Log.error("任务执行异常: " + e.getMessage());
        } finally {
            // 优雅关闭线程池
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}