package com.winsun.fruitmix.callback;

/**
 * Created by Administrator on 2018/1/19.
 */

public class BaseOperateCallbackWrapper extends BaseDataCallbackWrapper implements BaseOperateCallback {

    private BaseOperateCallback mBaseOperateCallback;

    public BaseOperateCallbackWrapper(BaseOperateCallback baseDataCallback, ActiveView activeView) {
        super(baseDataCallback, activeView);

        mBaseOperateCallback = baseDataCallback;
    }

    @Override
    public void onSucceed() {

        if (!activeView.isActive())
            return;

        mBaseOperateCallback.onSucceed();
    }
}
