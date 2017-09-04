package com.winsun.fruitmix.model.operationResult;

import android.content.Context;

import com.winsun.fruitmix.model.OperationResultType;

/**
 * Created by Administrator on 2017/8/18.
 */

public class OperationMediaDataChanged extends OperationResult {
    @Override
    public String getResultMessage(Context context) {
        return "";
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.MEDIA_DATA_CHANGED;
    }
}
