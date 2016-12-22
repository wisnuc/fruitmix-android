package com.winsun.fruitmix.operationResult;

import android.content.Context;

import com.winsun.fruitmix.util.OperationResultType;

/**
 * Created by Administrator on 2016/12/22.
 */

public class OperationNoChanged extends OperationResult {
    @Override
    public String getResultMessage(Context context) {
        return "";
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.NO_CHANGED;
    }
}
