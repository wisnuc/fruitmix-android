package com.winsun.fruitmix.business.callback;

import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2017/2/7.
 */

public interface LoadDeviceIdOperationCallback {

    interface LoadDeviceIDCallback{

        void onLoadSucceed(OperationResult result, String deviceID);

        void onLoadFail(OperationResult result);

    }

}
