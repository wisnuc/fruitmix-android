package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;

/**
 * Created by Administrator on 2017/2/15.
 */

public class LoggedInUserRequestEvent extends RequestEvent {

    private LoggedInUser mLoggedInUser;

    public LoggedInUserRequestEvent(OperationType operationType, OperationTargetType operationTargetType,LoggedInUser loggedInUser) {
        super(operationType, operationTargetType);
        mLoggedInUser = loggedInUser;
    }

    public LoggedInUser getmLoggedInUser() {
        return mLoggedInUser;
    }
}
