package com.winsun.fruitmix.user.datasource;

import android.content.Context;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.inject.Inject;

/**
 * Created by Administrator on 2017/7/24.
 */

public class InjectUser {

    public static UserDataRepository provideRepository(Context context) {

        return UserDataRepository.getInstance(new UserDBDataSourceImpl(DBUtils.getInstance(context)),
                new UserRemoteDataSourceImpl(Inject.provideIHttpUtil(context), Inject.provideHttpRequestFactory()));

    }


}
