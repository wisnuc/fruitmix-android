package com.winsun.fruitmix.operationResult;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.model.ErrorCode;
import com.winsun.fruitmix.util.OperationResultType;

/**
 * Created by Administrator on 2016/11/23.
 */

public class OperationMalformedUrlException extends OperationResult {
    @Override
    public String getResultMessage(Context context) {
        return String.format(context.getString(R.string.network_exception), ErrorCode.ERR_URL_PARAMETER_ILLEGAL.getCode());
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.MALFORMED_URL_EXCEPTION;
    }
}
