package com.dataflux.worker;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class TaskQueue {

    private final ThreadPoolExecutor executor;

    private static final int CORE_SIZE = 10;
    private static final int MAX_SIZE = 20;
    private static final int QUEUE_SIZE = 100;
    private static final int MAX_RETRIES = 3;
    private static final long WAIT_TIME = 200; // ms

    public TaskQueue() {

        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(QUEUE_SIZE);

        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "dataflux-worker-" + counter.incrementAndGet());
            }
        };

        this.executor = new ThreadPoolExecutor(
                CORE_SIZE,
                MAX_SIZE,
                60,
                TimeUnit.SECONDS,
                queue,
                factory,
                new ThreadPoolExecutor.AbortPolicy() // we handle retry
        );
    }

    public void addTask(Runnable task) {

        int retry = 0;
        long backoff = WAIT_TIME;

        while (retry < MAX_RETRIES) {
            try {
                executor.execute(task);
                return;

            } catch (RejectedExecutionException ex) {

                retry++;
                log.warn("Queue full, retrying... attempt={}", retry);

                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }

                backoff = Math.min(backoff * 2, 2000);
            }
        }

        //  final fallback (blocking)
        try {
            log.warn("Queue full, blocking until space available...");
            executor.getQueue().put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down TaskQueue...");
        executor.shutdown();
    }

    public Executor getExecutor() {
    return executor;
}
}
