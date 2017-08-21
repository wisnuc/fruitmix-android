package com.winsun.fruitmix.logged.in.user;

import com.winsun.fruitmix.group.data.source.FakeGroupDataSource;
import com.winsun.fruitmix.user.User;

import java.util.Collection;

/**
 * Created by Administrator on 2017/7/24.
 */

public class FakeLoggedInUserRepository implements LoggedInUserDataSource{

    public static final FakeLoggedInUserRepository instance = new FakeLoggedInUserRepository();

    @Override
    public boolean insertLoggedInUsers(Collection<LoggedInUser> loggedInUsers) {
        return false;
    }

    @Override
    public boolean deleteLoggedInUsers(Collection<LoggedInUser> loggedInUsers) {
        return false;
    }

    @Override
    public boolean clear() {
        return false;
    }

    @Override
    public Collection<LoggedInUser> getAllLoggedInUsers() {
        return null;
    }

    @Override
    public LoggedInUser getLoggedInUserByUserUUID(String userUUID) {
        User user = new User();
        user.setUuid(FakeGroupDataSource.MYSELF_UUID);

        return new LoggedInUser("","","","",user);
    }

}
