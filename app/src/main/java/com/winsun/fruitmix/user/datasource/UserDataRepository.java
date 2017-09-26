package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.user.User;

import java.util.Collection;

/**
 * Created by Administrator on 2017/7/28.
 */

public interface UserDataRepository extends UserRemoteDataSource{

    void setCacheDirty();

    boolean clearAllUsersInDB();

    User getUserByUUID(String userUUID);

    void insertUsers(Collection<User> users);

}
