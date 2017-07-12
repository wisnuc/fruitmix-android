package com.winsun.fruitmix.callback;

import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.List;

/**
 * Created by Administrator on 2017/7/7.
 */

public interface BaseLoadDataCallback<T> {

    void onSucceed(List<T> data, OperationResult operationResult);

    void onFail(OperationResult operationResult);

}
