package com.winsun.fruitmix.model.operationResult;

import android.content.Context;

import com.winsun.fruitmix.model.OperationResultType;

/**
 * Created by Administrator on 2017/8/18.
 */

public class OperationHasNewMedia extends OperationResult {
    @Override
    public String getResultMessage(Context context) {
        return "";
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.HAS_NEW_MEDIA;
    }
}
