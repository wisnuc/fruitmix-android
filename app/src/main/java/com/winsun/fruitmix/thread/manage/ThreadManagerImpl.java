package com.winsun.fruitmix.thread.manage;

import android.os.Handler;
import android.os.Looper;

import com.winsun.fruitmix.executor.ExecutorServiceInstance;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2017/7/12.
 */

public class ThreadManagerImpl implements ThreadManager {

    private static ThreadManagerImpl instance;

    private Handler handler;

    private ExecutorServiceInstance executorServiceInstance;

    private ThreadManagerImpl() {

        handler = new Handler(Looper.getMainLooper());

        executorServiceInstance = ExecutorServiceInstance.SINGLE_INSTANCE;
    }

    public static ThreadManagerImpl getInstance() {

        if (instance == null)
            instance = new ThreadManagerImpl();

        return instance;
    }

    @Override
    public void runOnCacheThread(Runnable runnable) {

        executorServiceInstance.doOneTaskInCachedThread(runnable);

    }

    @Override
    public void runOnCacheThread(Callable<Boolean> callable) {
        executorServiceInstance.doOneTaskInCachedThreadUsingCallable(callable);
    }

    @Override
    public void runOnGenerateThumbThread(Runnable runnable) {
        executorServiceInstance.doOneTaskInGenerateThumbThreadPool(runnable);
    }

    @Override
    public void stopGenerateThumbThreadNow() {
        executorServiceInstance.shutdownGenerateThumbThreadPoolNow();
    }

    @Override
    public void runOnGenerateMiniThumbThread(Callable<Boolean> callable) {
        executorServiceInstance.doOneTaskInGenerateMiniThumbThreadPool(callable);
    }

    @Override
    public void stopGenerateMiniThumbThreadNow() {
        executorServiceInstance.shutdownGenerateMiniThumbThreadPoolNow();
    }

    @Override
    public void runOnUploadMediaThread(Callable<Boolean> callable) {
        executorServiceInstance.doOneTaskInUploadMediaThreadPool(callable);
    }

    @Override
    public void runOnUploadMediaThread(Runnable runnable) {
        executorServiceInstance.doOneTaskInUploadMediaThreadPool(runnable);
    }

    @Override
    public Future<Boolean> runOnDownloadFileThread(Callable<Boolean> callable) {
        return executorServiceInstance.doOneTaskInDownloadFileThreadPool(callable);
    }

    @Override
    public void stopUploadMediaThreadNow() {
        executorServiceInstance.shutdownUploadMediaThreadPoolNow();
    }

    @Override
    public void stopUploadMediaThread() {
        executorServiceInstance.shutdownUploadMediaThreadPool();
    }

    @Override
    public void runOnMainThread(Runnable runnable) {
        handler.post(runnable);
    }


}
