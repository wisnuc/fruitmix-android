package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;

/**
 * Created by Administrator on 2017/1/18.
 */

public class UserRequestEvent extends RequestEvent {

    private String mUserName;
    private String mUserPassword;

    public UserRequestEvent(OperationType operationType, OperationTargetType operationTargetType,String userName,String userPassword) {
        super(operationType, operationTargetType);

        mUserName = userName;
        mUserPassword = userPassword;
    }

    public String getmUserName() {
        return mUserName;
    }

    public String getmUserPassword() {
        return mUserPassword;
    }
}
