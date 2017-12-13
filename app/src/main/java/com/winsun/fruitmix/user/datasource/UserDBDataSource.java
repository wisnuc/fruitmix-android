package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/7/7.
 */

public interface UserDBDataSource  {

    List<User> getUsers();

    OperationResult insertUser(Collection<User> users);

    boolean clearUsers();

    OperationResult modifyUserName(String userUUID,String userName);

    OperationResult modifyUserIsAdminState(String userUUID,boolean isAdmin);

    boolean updateUser(User user);

}
