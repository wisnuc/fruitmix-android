package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/7/7.
 */

public interface BaseUserDataSource {

    void getUsers(BaseLoadDataCallback<User> callback);

}
