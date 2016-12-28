package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;

/**
 * Created by Administrator on 2016/11/9.
 */

public class TokenRequestEvent extends RequestEvent {

    private String gateway;
    private String userUUID;
    private String userPassword;

    public TokenRequestEvent(OperationType operationType, OperationTargetType operationTargetType, String gateway, String userUUID, String userPassword) {
        super(operationType, operationTargetType);
        this.gateway = gateway;
        this.userUUID = userUUID;
        this.userPassword = userPassword;
    }

    public String getGateway() {
        return gateway;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public String getUserPassword() {
        return userPassword;
    }

}
