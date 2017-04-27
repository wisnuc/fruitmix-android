package com.winsun.fruitmix.model.operationResult;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.model.OperationResultType;

/**
 * Created by Administrator on 2016/11/23.
 */

public class OperationSuccess extends OperationResult {

    private int mOperationResID;

    public OperationSuccess(int operationResID) {
        mOperationResID = operationResID;
    }

    @Override
    public String getResultMessage(Context context) {
        return String.format(context.getString(R.string.success), context.getString(mOperationResID));
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.SUCCEED;
    }
}
