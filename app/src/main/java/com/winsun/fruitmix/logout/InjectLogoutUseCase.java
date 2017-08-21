package com.winsun.fruitmix.logout;

import android.content.Context;

import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;

/**
 * Created by Administrator on 2017/7/28.
 */

public class InjectLogoutUseCase {

    public static LogoutUseCase provideLogoutUseCase(Context context) {

        return LogoutUseCase.getInstance(InjectSystemSettingDataSource.provideSystemSettingDataSource(context));

    }

}
