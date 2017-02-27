package com.winsun.fruitmix.refactor.data.dataOperationResult;

import com.winsun.fruitmix.model.User;

import java.util.List;

/**
 * Created by Administrator on 2017/2/8.
 */

public class UsersLoadOperationResult extends DataOperationResult {

    private List<User> users;

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
