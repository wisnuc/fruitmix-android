package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;

/**
 * Created by Administrator on 2016/10/27.
 */

public class OperationEvent {

    private String action;
    private OperationResult operationResult;

    public OperationEvent(String action, OperationResult operationResult) {
        this.action = action;
        this.operationResult = operationResult;
    }

    public OperationEvent() {
    }

    public OperationResult getOperationResult() {
        return operationResult;
    }

    public String getAction() {
        return action;
    }
}
