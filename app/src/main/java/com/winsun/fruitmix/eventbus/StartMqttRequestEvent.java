package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2018/2/9.
 */

public class StartMqttRequestEvent extends RequestEvent {

    private String currentUserGUID;

    public StartMqttRequestEvent(OperationType operationType, OperationTargetType operationTargetType, String currentUserGUID) {
        super(operationType, operationTargetType);
        this.currentUserGUID = currentUserGUID;
    }

    public String getCurrentUserGUID() {
        return currentUserGUID;
    }

}
