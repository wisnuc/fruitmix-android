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

    private ExecutorService cacheThreadPool;

    private ExecutorService updateMediaThreadPool;

    private ExecutorService generateMiniThumbThreadPool;

    private ExecutorService generateThumbThreadPool;

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

    public <V> Future<V> doOneTaskInUploadMediaThreadPool(Callable<V> callable) {

        if (updateMediaThreadPool == null || updateMediaThreadPool.isShutdown())
            startUploadMediaThreadPool();

        return updateMediaThreadPool.submit(callable);
    }

    public void doOnTaskInUploadMediaThreadPool(Runnable runnable) {

        if (updateMediaThreadPool == null || updateMediaThreadPool.isShutdown())
            startUploadMediaThreadPool();

        updateMediaThreadPool.execute(runnable);
    }

    private void startUploadMediaThreadPool() {
        updateMediaThreadPool = Executors.newFixedThreadPool(THREAD_SIZE);
    }

    public void doOnTaskInGenerateMiniThumbThreadPool(Runnable runnable) {

        if (generateMiniThumbThreadPool == null || generateMiniThumbThreadPool.isShutdown())
            startGenerateMiniThumbThreadPool();

        generateMiniThumbThreadPool.execute(runnable);
    }

    private void startGenerateMiniThumbThreadPool() {
        generateMiniThumbThreadPool = Executors.newFixedThreadPool(THREAD_SIZE);
    }

    public void doOnTaskInGenerateThumbThreadPool(Runnable runnable) {

        if (generateThumbThreadPool == null || generateThumbThreadPool.isShutdown())
            startGenerateThumbThreadPool();

        generateThumbThreadPool.execute(runnable);
    }

    private void startGenerateThumbThreadPool() {
        generateThumbThreadPool = Executors.newFixedThreadPool(THREAD_SIZE);
    }


    public void shutdownUploadMediaThreadPool() {
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

    public void shutdownGenerateThumbThreadPoolNow() {
        if (generateThumbThreadPool != null && !generateThumbThreadPool.isShutdown()) {
            generateThumbThreadPool.shutdownNow();
            generateThumbThreadPool = null;
        }
    }

}
