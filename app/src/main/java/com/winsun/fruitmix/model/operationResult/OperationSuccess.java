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

    public OperationSuccess() {
    }

    @Override
    public String getResultMessage(Context context) {

        String message;

        if (mOperationResID == 0) {
            message = String.format(context.getString(R.string.success), context.getString(R.string.operate));
        } else {
            message = String.format(context.getString(R.string.success), context.getString(mOperationResID));
        }

        return message;
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.SUCCEED;
    }
}
