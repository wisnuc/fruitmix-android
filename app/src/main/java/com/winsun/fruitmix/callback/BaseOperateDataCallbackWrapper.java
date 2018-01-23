package com.winsun.fruitmix.callback;

import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2017/11/14.
 */

public class BaseOperateDataCallbackWrapper<T> extends BaseDataCallbackWrapper implements BaseOperateDataCallback<T> {

    private BaseOperateDataCallback<T> callback;

    public BaseOperateDataCallbackWrapper(BaseOperateDataCallback<T> callback, ActiveView activeView) {
        super(callback,activeView);
        this.callback = callback;
    }

    @Override
    public void onSucceed(T data, OperationResult result) {

        if (!activeView.isActive())
            return;

        callback.onSucceed(data, result);

    }



}
