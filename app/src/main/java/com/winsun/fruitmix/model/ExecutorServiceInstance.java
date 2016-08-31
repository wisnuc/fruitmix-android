package com.winsun.fruitmix.model;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/8/30.
 */
public enum ExecutorServiceInstance {

    SINGLE_INSTANCE;

    private ExecutorService executorService;

    ExecutorServiceInstance(){
        executorService = Executors.newCachedThreadPool();
    }

    public void doOneTaskInCachedThread(Runnable runnable) {
        executorService.execute(runnable);
    }
}
