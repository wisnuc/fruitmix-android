package com.winsun.fruitmix.logged.in.user;

import com.winsun.fruitmix.model.LoggedInUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Administrator on 2017/7/6.
 */

public class LoggedInUserRepository implements LoggedInUserDataSource {

    private Collection<LoggedInUser> cacheLoggedInUsers;

    private LoggedInUserDataSource loggedInUserDBDataSource;

    private boolean loadedFromDB = false;

    public LoggedInUserRepository(LoggedInUserDataSource loggedInUserDBDataSource) {
        this.loggedInUserDBDataSource = loggedInUserDBDataSource;

        cacheLoggedInUsers = new ArrayList<>();
    }

    Collection<LoggedInUser> getCacheLoggedInUsers() {
        return cacheLoggedInUsers;
    }

    void setCacheLoggedInUsers(Collection<LoggedInUser> cacheLoggedInUsers) {
        this.cacheLoggedInUsers = cacheLoggedInUsers;
    }

    @Override
    public boolean insertLoggedInUsers(Collection<LoggedInUser> loggedInUsers) {

        cacheLoggedInUsers.addAll(loggedInUsers);

        return loggedInUserDBDataSource.insertLoggedInUsers(loggedInUsers);

    }

    @Override
    public boolean deleteLoggedInUsers(Collection<LoggedInUser> loggedInUsers) {

        cacheLoggedInUsers.removeAll(loggedInUsers);

        return loggedInUserDBDataSource.deleteLoggedInUsers(loggedInUsers);
    }

    @Override
    public boolean clear() {
        return false;
    }

    @Override
    public Collection<LoggedInUser> getAllLoggedInUsers() {

        if (!loadedFromDB) {

            Collection<LoggedInUser> loggedInUsers = loggedInUserDBDataSource.getAllLoggedInUsers();

            cacheLoggedInUsers.addAll(loggedInUsers);

            loadedFromDB = true;
        }

        return cacheLoggedInUsers;
    }


}
