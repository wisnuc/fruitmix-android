package com.winsun.fruitmix.mock;

import com.winsun.fruitmix.thread.manage.ThreadManager;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2017/8/23.
 */

public class MockThreadManager implements ThreadManager {

    @Override
    public void runOnCacheThread(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void runOnCacheThread(Callable<Boolean> callable) {
        try {
            callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void runOnGenerateThumbThread(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void stopGenerateThumbThreadNow() {

    }

    @Override
    public void runOnGenerateMiniThumbThread(Callable<Boolean> callable) {
        try {
            callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopGenerateMiniThumbThreadNow() {

    }

    @Override
    public void runOnUploadMediaThread(Callable<Boolean> callable) {
        try {
            callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void runOnUploadMediaThread(Runnable runnable) {

        runnable.run();
    }

    @Override
    public Future<Boolean> runOnDownloadFileThread(Callable<Boolean> callable) {
        try {
            callable.call();

            return null;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void stopUploadMediaThreadNow() {

    }

    @Override
    public void stopUploadMediaThread() {

    }

    @Override
    public void runOnMainThread(Runnable runnable) {
        runnable.run();
    }
}
