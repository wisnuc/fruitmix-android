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

    public static final String TAG = ExecutorServiceInstance.class.getSimpleName();

    private ExecutorService cacheThreadPool;

    private ExecutorService updateMediaThreadPool;

    private ExecutorService generateMiniThumbThreadPool;

    private static final int THREAD_SIZE = 5;

    ExecutorServiceInstance() {
        cacheThreadPool = Executors.newCachedThreadPool();
    }

    public void doOneTaskInCachedThread(Runnable runnable) {
        cacheThreadPool.execute(runnable);
    }

    public <V> Future<V> doOneTaskInCachedThreadUsingCallable(Callable<V> callable) {
        return cacheThreadPool.submit(callable);
    }

    public <V> Future<V> doOneTaskInUploadThreadPool(Callable<V> callable) {

        return updateMediaThreadPool.submit(callable);
    }

    public void doOnTaskInUploadThreadPool(Runnable runnable) {

        if (updateMediaThreadPool != null && !updateMediaThreadPool.isShutdown())
            updateMediaThreadPool.execute(runnable);
    }

    public void doOnTaskInGenerateMiniThumbThreadPool(Runnable runnable) {

        if (generateMiniThumbThreadPool != null && !generateMiniThumbThreadPool.isShutdown())
            generateMiniThumbThreadPool.execute(runnable);
    }

    public void startGenerateMiniThumbThreadPool() {

        if (generateMiniThumbThreadPool == null || generateMiniThumbThreadPool.isShutdown())
            generateMiniThumbThreadPool = Executors.newFixedThreadPool(THREAD_SIZE);
    }

    public void startUploadThreadPool() {

        if (updateMediaThreadPool == null || updateMediaThreadPool.isShutdown())
            updateMediaThreadPool = Executors.newFixedThreadPool(THREAD_SIZE);
    }

    public void shutdownUploadThreadPool() {
        if (updateMediaThreadPool != null && !updateMediaThreadPool.isShutdown()) {
            updateMediaThreadPool.shutdown();
            updateMediaThreadPool = null;
        }
    }

    public void shutdownUploadMediaThreadPoolNow() {
        if (updateMediaThreadPool != null && !updateMediaThreadPool.isShutdown()) {
            updateMediaThreadPool.shutdownNow();
            updateMediaThreadPool = null;
        }
    }

    public void shutdownGenerateMiniThumbThreadPoolNow() {
        if (generateMiniThumbThreadPool != null && !generateMiniThumbThreadPool.isShutdown()) {
            generateMiniThumbThreadPool.shutdownNow();
            generateMiniThumbThreadPool = null;
        }
    }
}
