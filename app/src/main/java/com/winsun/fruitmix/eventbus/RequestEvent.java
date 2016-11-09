package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;

/**
 * Created by Administrator on 2016/11/9.
 */

public class RequestEvent {

    private OperationType operationType;
    private OperationTargetType operationTargetType;

    public RequestEvent(OperationType operationType, OperationTargetType operationTargetType) {
        this.operationType = operationType;
        this.operationTargetType = operationTargetType;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public OperationTargetType getOperationTargetType() {
        return operationTargetType;
    }
}
