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

    private ExecutorService downloadFileThreadPool;

    private ExecutorService uploadFileThreadPool;

    private static final int THREAD_SIZE = 4;

    private static final int UPLOAD_DOWNLOAD_THREAD_SIZE = 3;

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

    public void doOneTaskInUploadMediaThreadPool(Runnable runnable) {

        if (updateMediaThreadPool == null || updateMediaThreadPool.isShutdown())
            startUploadMediaThreadPool();

        updateMediaThreadPool.execute(runnable);
    }

    private void startUploadMediaThreadPool() {
        updateMediaThreadPool = Executors.newFixedThreadPool(1);
    }

    public void doOneTaskInGenerateMiniThumbThreadPool(Callable<Boolean> callable) {

        if (generateMiniThumbThreadPool == null || generateMiniThumbThreadPool.isShutdown())
            startGenerateMiniThumbThreadPool();

        generateMiniThumbThreadPool.submit(callable);
    }

    private void startGenerateMiniThumbThreadPool() {
        generateMiniThumbThreadPool = Executors.newFixedThreadPool(THREAD_SIZE);
    }

    public void doOneTaskInGenerateThumbThreadPool(Runnable runnable) {

        if (generateThumbThreadPool == null || generateThumbThreadPool.isShutdown())
            startGenerateThumbThreadPool();

        generateThumbThreadPool.execute(runnable);
    }

    private void startGenerateThumbThreadPool() {
        generateThumbThreadPool = Executors.newFixedThreadPool(THREAD_SIZE);
    }

    public Future<Boolean> doOneTaskInDownloadFileThreadPool(Callable<Boolean> callable) {

        if (downloadFileThreadPool == null || downloadFileThreadPool.isShutdown())
            startDownloadFileThreadPool();

        return downloadFileThreadPool.submit(callable);

    }

    private void startDownloadFileThreadPool() {
        downloadFileThreadPool = Executors.newFixedThreadPool(UPLOAD_DOWNLOAD_THREAD_SIZE);
    }

    public Future<Boolean> doOneTaskInUploadFileThreadPool(Callable<Boolean> callable) {

        if (uploadFileThreadPool == null || uploadFileThreadPool.isShutdown())
            startUploadFileThreadPool();

        return uploadFileThreadPool.submit(callable);

    }

    private void startUploadFileThreadPool() {
        uploadFileThreadPool = Executors.newFixedThreadPool(UPLOAD_DOWNLOAD_THREAD_SIZE);
    }

    public void shutdownUploadFileThreadPoolNow() {
        if (uploadFileThreadPool != null && !uploadFileThreadPool.isShutdown()) {
            uploadFileThreadPool.shutdownNow();
            uploadFileThreadPool = null;
        }
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

    public void shutdownGenerateMiniThumbThreadPool() {
        if (generateMiniThumbThreadPool != null && !generateMiniThumbThreadPool.isShutdown()) {
            generateMiniThumbThreadPool.shutdown();
            generateMiniThumbThreadPool = null;
        }
    }

    public void shutdownGenerateThumbThreadPool() {
        if (generateThumbThreadPool != null && !generateThumbThreadPool.isShutdown()) {
            generateThumbThreadPool.shutdown();
            generateThumbThreadPool = null;
        }
    }

}
