package com.winsun.fruitmix.logged.in.user;

import android.content.Context;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/7/24.
 */

public class InjectLoggedInUser {

    public static LoggedInUserDataSource provideLoggedInUserRepository(Context context) {

        return LoggedInUserRepository.getInstance(ThreadManagerImpl.getInstance(),
                LoggedInUserDBDataSource.getInstance(DBUtils.getInstance(context)),
                new LoggedInUserRemoteDataSourceImpl(InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory(context)));

//        return FakeLoggedInUserRepository.instance;

    }

}
