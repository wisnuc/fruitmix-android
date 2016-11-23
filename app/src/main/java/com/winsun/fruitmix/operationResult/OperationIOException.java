package com.winsun.fruitmix.operationResult;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.util.OperationResultType;

/**
 * Created by Administrator on 2016/11/23.
 */

public class OperationIOException extends OperationResult {

    @Override
    public String getResultMessage(Context context) {
        return context.getString(R.string.io_exception);
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.IO_EXCEPTION;
    }
}
