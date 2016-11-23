package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.util.OperationResultType;

/**
 * Created by Administrator on 2016/11/1.
 */

public class OperationEvent {

    private String action;
    private OperationResultType operationResultType;

    public OperationEvent(String action, OperationResultType operationResultType) {
        this.action = action;
        this.operationResultType = operationResultType;
    }

    public OperationResultType getOperationResultType() {
        return operationResultType;
    }

    public String getAction() {
        return action;
    }

}
