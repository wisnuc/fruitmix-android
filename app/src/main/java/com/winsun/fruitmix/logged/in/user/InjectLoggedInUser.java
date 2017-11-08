package com.winsun.fruitmix.logged.in.user;

import android.content.Context;

import com.winsun.fruitmix.db.DBUtils;

/**
 * Created by Administrator on 2017/7/24.
 */

public class InjectLoggedInUser {

    public static LoggedInUserDataSource provideLoggedInUserRepository(Context context) {

        return LoggedInUserRepository.getInstance(LoggedInUserDBDataSource.getInstance(DBUtils.getInstance(context)));

//        return FakeLoggedInUserRepository.instance;
    }

}
