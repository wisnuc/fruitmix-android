package com.winsun.fruitmix.logout;

import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;

import java.util.Collections;

/**
 * Created by Administrator on 2017/7/28.
 */

public class LogoutUseCase {

    private static LogoutUseCase ourInstance;

    private LoggedInUserDataSource loggedInUserDataSource;

    public static LogoutUseCase getInstance(LoggedInUserDataSource loggedInUserDataSource) {
        if (ourInstance == null)
            ourInstance = new LogoutUseCase(loggedInUserDataSource);
        return ourInstance;
    }

    private LogoutUseCase(LoggedInUserDataSource loggedInUserDataSource) {

        this.loggedInUserDataSource = loggedInUserDataSource;
    }

    public void logout() {

        LoggedInUser loggedInUser = loggedInUserDataSource.getCurrentLoggedInUser();

        loggedInUserDataSource.deleteLoggedInUsers(Collections.singletonList(loggedInUser));

        loggedInUserDataSource.setCurrentLoggedInUser(null);

    }


}
