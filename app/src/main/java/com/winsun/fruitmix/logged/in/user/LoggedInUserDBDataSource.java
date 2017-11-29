package com.winsun.fruitmix.logged.in.user;

import android.content.Context;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;

import java.util.Collection;

/**
 * Created by Administrator on 2017/7/4.
 */

public class LoggedInUserDBDataSource implements LoggedInUserDBSource {

    private DBUtils dbUtils;

    private static LoggedInUserDBSource instance;

    public static LoggedInUserDBSource getInstance(DBUtils dbUtils) {

        if (instance == null)
            instance = new LoggedInUserDBDataSource(dbUtils);

        return instance;
    }

    private LoggedInUserDBDataSource(DBUtils dbUtils) {

        this.dbUtils = dbUtils;

    }

    @Override
    public boolean insertLoggedInUsers(Collection<LoggedInUser> loggedInUsers) {

        long result = dbUtils.insertLoggedInUserInDB(loggedInUsers);

        return result > 0;
    }

    @Override
    public boolean deleteLoggedInUsers(Collection<LoggedInUser> loggedInUsers) {

        long result = 0;

        for (LoggedInUser loggedInUser : loggedInUsers) {
            result = dbUtils.deleteLoggerUserByUserUUID(loggedInUser.getUser().getUuid());
        }

        return result > 0;
    }

    @Override
    public boolean clear() {

        return dbUtils.deleteAllLoggedInUser() > 0;
    }

    @Override
    public Collection<LoggedInUser> getAllLoggedInUsers() {

        return dbUtils.getAllLoggedInUser();
    }

    @Override
    public LoggedInUser getLoggedInUserByUserUUID(String userUUID) {

        return dbUtils.getCurrentLoggedInUserByUUID(userUUID);

    }

    @Override
    public LoggedInUser getLoggedInUserByToken(String token) {

        return dbUtils.getCurrentLoggedInUserByToken(token);
    }
}
