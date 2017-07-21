package com.winsun.fruitmix.callback;

import android.util.Log;

import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2017/7/13.
 */

public class BaseOperateDataCallbackImpl<T> implements BaseOperateDataCallback<T> {

    public static final String TAG = BaseOperateDataCallbackImpl.class.getSimpleName();


    @Override
    public void onSucceed(T data, OperationResult result) {

    }

    @Override
    public void onFail(OperationResult result) {
        Log.d(TAG, "onFail: result: " + result);
    }
}
