package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.Collection;

/**
 * Created by Administrator on 2017/7/7.
 */

public interface UserDBDataSource  {

    void getUsers(BaseLoadDataCallback<User> callback);

    OperationResult insertUser(Collection<User> users);

    boolean clearUsers();
}
