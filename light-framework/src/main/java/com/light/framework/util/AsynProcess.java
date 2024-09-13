package com.light.framework.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsynProcess {
    private static final Logger logger = LoggerFactory.getLogger(AsynProcess.class);

    private final CountDownLatch latch;

    private final Map<String, FutureTask<Void>> futures;

    public AsynProcess(int num) {
        this.latch = new CountDownLatch(num);
        this.futures = new HashMap<>(num);
    }

    public void execute(String taskName, final AsynHandle asynHandle) {
        FutureTask<Void> task = new FutureTask<>(() -> {
            try {
                asynHandle.process();
            } finally {
                AsynProcess.this.latch.countDown();
            }
            return null;
        });
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
        this.futures.put(taskName, task);
    }

    public void awaitAndfinish() {
        try {
            this.latch.await(5L, TimeUnit.SECONDS);
        } catch (InterruptedException interruptedException) {
        }
        for (Map.Entry<String, FutureTask<Void>> entry : this.futures.entrySet()) {
            logger.info(entry.getKey() + ": " + (entry.getValue().isDone() ? "释放完成" : "未释放完成"));
        }
    }

    public static interface AsynHandle {
        void process();
    }
}