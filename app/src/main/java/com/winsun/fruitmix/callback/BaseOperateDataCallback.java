package com.winsun.fruitmix.callback;

import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2017/7/7.
 */

public interface BaseOperateDataCallback<T> extends BaseDataCallback{

    void onSucceed(T data, OperationResult result);

}
