package com.winsun.fruitmix.callback;

import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2017/11/14.
 */

public class BaseOperateDataCallbackWrapper<T> implements BaseOperateDataCallback<T> {

    private BaseOperateDataCallback<T> callback;

    private ActiveView activeView;

    public BaseOperateDataCallbackWrapper(BaseOperateDataCallback<T> callback, ActiveView activeView) {
        this.callback = callback;
        this.activeView = activeView;
    }

    @Override
    public void onFail(OperationResult operationResult) {

        if (!activeView.isActive())
            return;

        callback.onFail(operationResult);

    }

    @Override
    public void onSucceed(T data, OperationResult result) {

        if (!activeView.isActive())
            return;

        callback.onSucceed(data, result);

    }
}
