package com.winsun.fruitmix.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2016/8/30.
 */
public enum ExecutorServiceInstance {

    SINGLE_INSTANCE;

    private ExecutorService executorService;

    private ExecutorService fixedThreadPool;

    private static final int THREAD_SIZE = 5;

    ExecutorServiceInstance() {
        executorService = Executors.newCachedThreadPool();
    }

    public void doOneTaskInCachedThread(Runnable runnable) {
        executorService.execute(runnable);
    }

    public <V> Future<V> doOneTaskInCachedTheadUsingCallable(Callable<V> callable){
        return executorService.submit(callable);
    }

    public <V> Future<V> doOneTaskInFixedThreadPool(Callable<V> callable) {
        return fixedThreadPool.submit(callable);
    }

    public void startFixedThreadPool(){
        fixedThreadPool = Executors.newFixedThreadPool(THREAD_SIZE);
    }

    public void shutdownFixedThreadPool() {
        if (!fixedThreadPool.isShutdown())
            fixedThreadPool.shutdown();
    }

    public void shutdownFixedThreadPoolNow() {
        if (!fixedThreadPool.isShutdown())
            fixedThreadPool.shutdownNow();
    }
}
