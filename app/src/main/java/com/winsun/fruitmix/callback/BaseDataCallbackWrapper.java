package com.winsun.fruitmix.callback;

import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2018/1/19.
 */

public class BaseDataCallbackWrapper implements BaseDataCallback {

    protected ActiveView activeView;

    private BaseDataCallback mBaseDataCallback;

    public BaseDataCallbackWrapper(BaseDataCallback baseDataCallback,ActiveView activeView) {
        this.activeView = activeView;
        mBaseDataCallback = baseDataCallback;
    }

    @Override
    public void onFail(OperationResult operationResult) {

        if (!activeView.isActive())
            return;

        mBaseDataCallback.onFail(operationResult);

    }
}
