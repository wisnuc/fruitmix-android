package com.winsun.fruitmix.refactor.common.callback;

import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2017/2/7.
 */

public interface LoadTokenOperationCallback {

    interface LoadTokenCallback{

        void onLoadSucceed(OperationResult result,String token);

        void onLoadFail(OperationResult result);

    }

}
