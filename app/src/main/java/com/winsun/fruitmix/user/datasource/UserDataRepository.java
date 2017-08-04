package com.winsun.fruitmix.user.datasource;

/**
 * Created by Administrator on 2017/7/28.
 */

public interface UserDataRepository extends UserRemoteDataSource{

    void setCacheDirty();

}
