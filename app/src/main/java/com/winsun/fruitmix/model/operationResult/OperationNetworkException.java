package com.winsun.fruitmix.model.operationResult;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.model.OperationResultType;

/**
 * Created by Administrator on 2016/11/23.
 */

public class OperationNetworkException extends OperationResult {

    private int responseCode = 0;

    public OperationNetworkException(int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String getResultMessage(Context context) {

        String resultMessage;

        if (responseCode == 401) {
            resultMessage = context.getString(R.string.password_error);
        } else {
            resultMessage = String.format(context.getString(R.string.network_exception), "http " + responseCode);
        }

        return resultMessage;
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.NETWORK_EXCEPTION;
    }
}
