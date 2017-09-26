package com.winsun.fruitmix.user.datasource;

import android.content.Context;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/7/24.
 */

public class InjectUser {

    public static UserDataRepository provideRepository(Context context) {

        return UserDataRepositoryImpl.getInstance(new UserDBDataSourceImpl(DBUtils.getInstance(context)),
                new UserRemoteDataSourceImpl(InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory(context),
                        InjectSystemSettingDataSource.provideSystemSettingDataSource(context)), ThreadManagerImpl.getInstance());

    }

}
