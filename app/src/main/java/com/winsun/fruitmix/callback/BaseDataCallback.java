package com.winsun.fruitmix.callback;

import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.io.IOException;

/**
 * Created by Administrator on 2017/10/29.
 */

public interface BaseDataCallback {

    void onFail(OperationResult operationResult);

}
