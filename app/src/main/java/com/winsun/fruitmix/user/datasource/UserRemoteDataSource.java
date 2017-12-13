package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/7/7.
 */

public interface UserRemoteDataSource {

    void getUsers(String currentLoginUserUUID, BaseLoadDataCallback<User> callback);

    void insertUser(String userName, String userPwd, BaseOperateDataCallback<User> callback);

    void getCurrentUserHome(BaseLoadDataCallback<String> callback);

    void getUsersByStationIDWithCloudAPI(String stationID, BaseLoadDataCallback<User> callback);

    void getUserDetailedInfoByUUID(String userUUID, BaseLoadDataCallback<User> callback);

    void getWeUserInfoByGUIDWithCloudAPI(String guid, BaseLoadDataCallback<User> callback);

    void modifyUserName(String userUUID,String userName, BaseOperateDataCallback<User> callback);

    void modifyUserPassword(String userUUID,String originalPassword,String newPassword, BaseOperateDataCallback<Boolean> callback);

    void modifyUserEnableState(String userUUID, boolean newState, BaseOperateDataCallback<User> callback);

    void modifyUserIsAdminState(String userUUID,boolean newIsAdminState,BaseOperateDataCallback<User> callback);

}
