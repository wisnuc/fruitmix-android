package com.winsun.fruitmix.model.operationResult;

import android.content.Context;

import com.winsun.fruitmix.model.OperationResultType;

/**
 * Created by Administrator on 2016/11/23.
 */

public abstract class OperationResult{

    public abstract String getResultMessage(Context context);

    public abstract OperationResultType getOperationResultType();

}
