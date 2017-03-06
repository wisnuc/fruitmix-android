package com.winsun.fruitmix.data.dataOperationResult;

import com.winsun.fruitmix.model.User;

/**
 * Created by Administrator on 2017/2/27.
 */

public class OperateUserResult extends DataOperationResult {

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
