package com.winsun.fruitmix.model.operationResult;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.model.ErrorCode;
import com.winsun.fruitmix.model.OperationResultType;

/**
 * Created by Administrator on 2016/11/23.
 */

public class OperationJSONException extends OperationResult {
    @Override
    public String getResultMessage(Context context) {
        return String.format(context.getString(R.string.network_exception), ErrorCode.ERR_NETWORK_DATA_PARSE_FAILED.getCode());
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.JSON_EXCEPTION;
    }
}
