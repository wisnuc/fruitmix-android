package com.winsun.fruitmix;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import java.util.List;

/**
 * Created by Administrator on 2017/8/28.
 */

public class BaseDataRepository {

    protected ThreadManager mThreadManager;

    public BaseDataRepository(ThreadManager threadManager) {
        this.mThreadManager = threadManager;
    }

    protected  <T> BaseOperateDataCallback<T> createOperateCallbackRunOnMainThread(final BaseOperateDataCallback<T> callback) {
        return new BaseOperateDataCallback<T>() {
            @Override
            public void onSucceed(final T data, final OperationResult result) {

                mThreadManager.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSucceed(data, result);
                    }
                });

            }

            @Override
            public void onFail(final OperationResult result) {

                mThreadManager.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFail(result);
                    }
                });

            }
        };
    }

    protected  <T> BaseLoadDataCallback<T> createLoadCallbackRunOnMainThread(final BaseLoadDataCallback<T> callback) {
        return new BaseLoadDataCallback<T>() {
            @Override
            public void onSucceed(final List<T> data, final OperationResult result) {

                mThreadManager.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSucceed(data, result);
                    }
                });

            }

            @Override
            public void onFail(final OperationResult result) {

                mThreadManager.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFail(result);
                    }
                });

            }
        };
    }

}
