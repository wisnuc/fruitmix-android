package com.winsun.fruitmix.model.operationResult;

import android.content.Context;

import com.winsun.fruitmix.model.OperationResultType;

/**
 * Created by Administrator on 2017/9/15.
 */

public class OperationFail extends OperationResult {

    private String failReason;
    private int failReasonResId;

    public OperationFail(String failReason) {
        this.failReason = failReason;
    }

    public OperationFail(int failReasonResId) {
        this.failReasonResId = failReasonResId;
    }

    @Override
    public String getResultMessage(Context context) {

        if (failReason != null)
            return failReason;
        else
            return context.getString(failReasonResId);
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.OPERATION_FAIL;
    }
}
