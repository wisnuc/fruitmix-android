package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/7/7.
 */

public interface UserRemoteDataSource {

    void getUsers(String currentLoginUserUUID,BaseLoadDataCallback<User> callback);

    void insertUser(String userName, String userPwd, BaseOperateDataCallback<User> callback);

    void getCurrentUserHome(BaseLoadDataCallback<String> callback);

    void getUsersByStationID(String stationID, BaseLoadDataCallback<User> callback);

    void getUserByUUID(String userUUID,BaseLoadDataCallback<User> callback);

}
