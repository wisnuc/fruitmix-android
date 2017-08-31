package com.winsun.fruitmix.logout;

import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.logged.in.user.LoggedInUserRepository;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;

import java.util.Collections;

/**
 * Created by Administrator on 2017/7/28.
 */

public class LogoutUseCase {

    private static LogoutUseCase ourInstance;

    private SystemSettingDataSource systemSettingDataSource;

    private LoggedInUserDataSource loggedInUserDataSource;

    public static LogoutUseCase getInstance(SystemSettingDataSource systemSettingDataSource, LoggedInUserDataSource loggedInUserDataSource) {
        if (ourInstance == null)
            ourInstance = new LogoutUseCase(systemSettingDataSource, loggedInUserDataSource);
        return ourInstance;
    }

    private LogoutUseCase(SystemSettingDataSource systemSettingDataSource, LoggedInUserDataSource loggedInUserDataSource) {

        this.systemSettingDataSource = systemSettingDataSource;
        this.loggedInUserDataSource = loggedInUserDataSource;
    }

    public void logout() {

        String currentLoginUserUUID = systemSettingDataSource.getCurrentLoginUserUUID();

        LoggedInUser loggedInUser = loggedInUserDataSource.getLoggedInUserByUserUUID(currentLoginUserUUID);

        loggedInUserDataSource.deleteLoggedInUsers(Collections.singletonList(loggedInUser));

        systemSettingDataSource.setCurrentLoginUserUUID("");
    }


}
