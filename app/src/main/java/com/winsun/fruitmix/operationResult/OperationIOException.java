package com.winsun.fruitmix.operationResult;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.model.ErrorCode;
import com.winsun.fruitmix.util.OperationResultType;

/**
 * Created by Administrator on 2016/11/23.
 */

public class OperationIOException extends OperationResult {

    @Override
    public String getResultMessage(Context context) {
        return String.format(context.getString(R.string.network_exception), ErrorCode.ERR_NOT_DEFINED.getCode());
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.IO_EXCEPTION;
    }
}
