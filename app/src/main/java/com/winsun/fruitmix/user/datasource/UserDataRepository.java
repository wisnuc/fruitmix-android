package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.user.User;

import java.util.Collection;

/**
 * Created by Administrator on 2017/7/28.
 */

public interface UserDataRepository extends UserRemoteDataSource{

    void setCacheDirty();

    boolean clearAllUsersInDB();

    void clearAllUsersInCache();

    User getUserByUUID(String userUUID);

    User getUserByGUID(String userGUID);

    void insertUsers(Collection<User> users);

    boolean updateUser(User user);

}
