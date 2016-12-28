package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2016/11/1.
 */

public class OperationEvent {

    private String action;
    private OperationResult operationResult;

    public OperationEvent(String action, OperationResult operationResult) {
        this.action = action;
        this.operationResult = operationResult;
    }

    public OperationResult getOperationResult() {
        return operationResult;
    }

    public String getAction() {
        return action;
    }

}
