package com.winsun.fruitmix.operationResult;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.util.OperationResultType;

/**
 * Created by Administrator on 2016/11/24.
 */

public class OperationFileNotFoundException extends OperationResult {
    @Override
    public String getResultMessage(Context context) {
        return context.getString(R.string.file_not_found_exception);
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.FILE_NOT_FOUND_EXCEPTION;
    }
}
