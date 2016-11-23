package com.winsun.fruitmix.operationResult;

import android.content.Context;

import com.winsun.fruitmix.util.OperationResultType;

/**
 * Created by Administrator on 2016/11/23.
 */

public abstract class OperationResult {

    public abstract String getResultMessage(Context context);

    public abstract OperationResultType getOperationResultType();

}
