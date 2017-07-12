package com.winsun.fruitmix.thread.manage;

import android.os.Handler;
import android.os.Looper;

import com.winsun.fruitmix.executor.ExecutorServiceInstance;

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

    public void runOnMainThread(Runnable runnable) {

        handler.post(runnable);
    }
}
