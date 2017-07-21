package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.Collection;

/**
 * Created by Administrator on 2017/7/7.
 */

public interface UserDBDataSource extends BaseUserDataSource {

    OperationResult insertUser(Collection<User> users);

    boolean clearUsers();
}
