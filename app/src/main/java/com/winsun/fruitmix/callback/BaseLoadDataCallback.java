package com.winsun.fruitmix.callback;

import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.List;

/**
 * Created by Administrator on 2017/7/7.
 */

public interface BaseLoadDataCallback<T> extends BaseDataCallback{

    void onSucceed(List<T> data, OperationResult operationResult);

}
