package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2017/7/7.
 */

public interface UserRemoteDataSource extends BaseUserDataSource {

    void insertUser(String userName, String userPwd, BaseOperateDataCallback<User> callback);

}
