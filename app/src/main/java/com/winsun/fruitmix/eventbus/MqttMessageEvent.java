package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2018/2/9.
 */

public class MqttMessageEvent extends OperationEvent {

    private String message;

    public MqttMessageEvent(String action, OperationResult operationResult, String message) {
        super(action, operationResult);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
