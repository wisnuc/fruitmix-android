package com.winsun.fruitmix.logged.in.user;

import java.util.Collection;

/**
 * Created by Administrator on 2017/11/29.
 */

public interface LoggedInUserDBSource {

    boolean insertLoggedInUsers(Collection<LoggedInUser> loggedInUsers);

    boolean deleteLoggedInUsers(Collection<LoggedInUser> loggedInUsers);

    boolean clear();

    Collection<LoggedInUser> getAllLoggedInUsers();

    LoggedInUser getLoggedInUserByUserUUID(String userUUID);

    LoggedInUser getLoggedInUserByToken(String token);

}
