package com.winsun.fruitmix.logged.in.user;

import com.winsun.fruitmix.group.data.source.FakeGroupDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Administrator on 2017/7/24.
 */

public class FakeLoggedInUserRepository implements LoggedInUserDataSource {

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

        Collection<LoggedInUser> loggedInUsers = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            LoggedInUser loggedInUser = new LoggedInUser();
            loggedInUser.setDeviceID("testDeviceID" + i);
            loggedInUser.setGateway("testGateway" + i);
            loggedInUser.setToken("testToken" + i);
            loggedInUser.setEquipmentName("testEquipmentName" + i);

            User user = new User();
            user.setUuid("testUserUUID" + i);
            user.setUserName("testUserName" + i);
            user.setDefaultAvatar(Util.getUserNameFirstLetter(user.getUserName()));

            loggedInUser.setUser(user);

            loggedInUsers.add(loggedInUser);
        }

        return loggedInUsers;
    }

    @Override
    public LoggedInUser getLoggedInUserByUserUUID(String userUUID) {
        User user = new User();
        user.setUuid(FakeGroupDataSource.MYSELF_UUID);

        return new LoggedInUser("", "", "", "", user);
    }

    @Override
    public LoggedInUser getLoggedInUserByToken(String token) {
        User user = new User();
        user.setUuid(FakeGroupDataSource.MYSELF_UUID);

        return new LoggedInUser("", "", "", "", user);
    }
}
