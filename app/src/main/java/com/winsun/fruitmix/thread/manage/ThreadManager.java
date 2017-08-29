package com.winsun.fruitmix.thread.manage;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2017/8/23.
 */

public interface ThreadManager {

    void runOnCacheThread(Runnable runnable);

    void runOnCacheThread(Callable<Boolean> callable);

    void runOnGenerateThumbThread(Runnable runnable);

    void stopGenerateThumbThreadNow();

    void runOnGenerateMiniThumbThread(Callable<Boolean> callable);

    void stopGenerateMiniThumbThreadNow();

    void runOnUploadMediaThread(Callable<Boolean> callable);

    void runOnUploadMediaThread(Runnable runnable);

    Future<Boolean> runOnDownloadFileThread(Callable<Boolean> callable);

    void stopUploadMediaThreadNow();

    void stopUploadMediaThread();

    void runOnMainThread(Runnable runnable);

}
