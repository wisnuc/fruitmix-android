package com.winsun.fruitmix.callback;

import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.List;

/**
 * Created by Administrator on 2017/11/14.
 */

public class BaseLoadDataCallbackWrapper<T> implements BaseLoadDataCallback<T> {

    private BaseLoadDataCallback<T> callback;

    private ActiveView activeView;

    public BaseLoadDataCallbackWrapper(BaseLoadDataCallback<T> callback, ActiveView activeView) {
        this.callback = callback;
        this.activeView = activeView;
    }

    @Override
    public void onSucceed(List<T> data, OperationResult operationResult) {

        if (!activeView.isActive())
            return;

        callback.onSucceed(data, operationResult);

    }

    @Override
    public void onFail(OperationResult operationResult) {

        if (!activeView.isActive())
            return;

        callback.onFail(operationResult);
    }

}
