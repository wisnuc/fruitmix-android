package com.winsun.fruitmix.thread.manage;

import android.os.Handler;
import android.os.Looper;

import com.winsun.fruitmix.executor.ExecutorServiceInstance;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2017/7/12.
 */

public class ThreadManager {

    private static ThreadManager instance;

    private Handler handler;

    private ExecutorServiceInstance executorServiceInstance;

    private ThreadManager() {

        handler = new Handler(Looper.getMainLooper());

        executorServiceInstance = ExecutorServiceInstance.SINGLE_INSTANCE;
    }

    public static ThreadManager getInstance() {

        if (instance == null)
            instance = new ThreadManager();

        return instance;
    }

    public void runOnCacheThread(Runnable runnable) {

        executorServiceInstance.doOneTaskInCachedThread(runnable);

    }

    public void runOnCacheThread(Callable<Boolean> callable) {

        executorServiceInstance.doOneTaskInCachedThreadUsingCallable(callable);
    }

    public void runOnGenerateThumbThread(Runnable runnable) {
        executorServiceInstance.doOneTaskInGenerateThumbThreadPool(runnable);
    }

    public void stopGenerateThumbThreadNow(){
        executorServiceInstance.shutdownGenerateThumbThreadPoolNow();
    }

    public void runOnGenerateMiniThumbThread(Callable<Boolean> callable) {
        executorServiceInstance.doOneTaskInGenerateMiniThumbThreadPool(callable);
    }

    public void stopGenerateMiniThumbThreadNow(){
        executorServiceInstance.shutdownGenerateMiniThumbThreadPoolNow();
    }

    public void runOnUploadMediaThread(Callable<Boolean> callable) {
        executorServiceInstance.doOneTaskInUploadMediaThreadPool(callable);
    }

    public void runOnUploadMediaThread(Runnable runnable){
        executorServiceInstance.doOneTaskInUploadMediaThreadPool(runnable);
    }

    public void stopUploadMediaThreadNow(){
        executorServiceInstance.shutdownUploadMediaThreadPoolNow();
    }

    public void stopUploadMediaThread(){
        executorServiceInstance.shutdownUploadMediaThreadPool();
    }

    public void runOnMainThread(Runnable runnable) {

        handler.post(runnable);
    }


}
