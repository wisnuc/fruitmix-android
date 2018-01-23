package com.winsun.fruitmix.callback;

import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.List;

/**
 * Created by Administrator on 2017/11/14.
 */

public class BaseLoadDataCallbackWrapper<T> extends BaseDataCallbackWrapper implements BaseLoadDataCallback<T> {

    private BaseLoadDataCallback<T> callback;

    public BaseLoadDataCallbackWrapper(BaseLoadDataCallback<T> callback, ActiveView activeView) {
        super(callback,activeView);
        this.callback = callback;

    }

    @Override
    public void onSucceed(List<T> data, OperationResult operationResult) {

        if (!activeView.isActive())
            return;

        callback.onSucceed(data, operationResult);

    }

}
