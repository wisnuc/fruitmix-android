package com.winsun.fruitmix.logged.in.user;

import com.winsun.fruitmix.model.LoggedInUser;

import java.util.Collection;

/**
 * Created by Administrator on 2017/7/4.
 */

public interface LoggedInUserDataSource {

    boolean insertLoggedInUsers(Collection<LoggedInUser> loggedInUsers);

    boolean deleteLoggedInUsers(Collection<LoggedInUser> loggedInUsers);

    boolean clear();

    Collection<LoggedInUser> getAllLoggedInUsers();

}
