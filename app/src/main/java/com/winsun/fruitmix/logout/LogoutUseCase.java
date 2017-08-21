package com.winsun.fruitmix.logout;

import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;

import java.util.Collections;

/**
 * Created by Administrator on 2017/7/28.
 */

public class LogoutUseCase {

    private static LogoutUseCase ourInstance;

    private SystemSettingDataSource systemSettingDataSource;

    public static LogoutUseCase getInstance(SystemSettingDataSource systemSettingDataSource) {
        if (ourInstance == null)
            ourInstance = new LogoutUseCase(systemSettingDataSource);
        return ourInstance;
    }

    private LogoutUseCase(SystemSettingDataSource systemSettingDataSource) {

        this.systemSettingDataSource = systemSettingDataSource;
    }

    public void logout() {

        systemSettingDataSource.setCurrentLoginUserUUID("");

    }


}
