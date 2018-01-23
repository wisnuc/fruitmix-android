package com.winsun.fruitmix.callback;

import android.util.Log;

import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2018/1/19.
 */

public class BaseDataCallbackImpl implements BaseDataCallback {

    private static final String TAG = BaseDataCallbackImpl.class.getSimpleName();

    @Override
    public void onFail(OperationResult operationResult) {
        Log.d(TAG, "onFail: result: " + operationResult);
    }
}
