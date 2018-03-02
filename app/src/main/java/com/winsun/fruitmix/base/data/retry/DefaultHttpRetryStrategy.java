package com.winsun.fruitmix.base.data.retry;

import com.winsun.fruitmix.http.DefaultRetryStrategy;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2018/3/1.
 */

public class DefaultHttpRetryStrategy extends DefaultRetryStrategy {

    private OperationResult mOperationResult;

    public DefaultHttpRetryStrategy() {
    }

    public DefaultHttpRetryStrategy(int mMaxRetryCount) {
        super(mMaxRetryCount);
    }

    public void setOperationResult(OperationResult operationResult) {
        mOperationResult = operationResult;
    }

    @Override
    public boolean needRetry() {

        if(mOperationResult != null && (mOperationResult.getOperationResultType() == OperationResultType.NETWORK_EXCEPTION)) {

            OperationNetworkException operationNetworkException = (OperationNetworkException) mOperationResult;

            return operationNetworkException.getHttpResponseCode() == 401 && super.needRetry();

        }else {
            return false;
        }

    }
}
