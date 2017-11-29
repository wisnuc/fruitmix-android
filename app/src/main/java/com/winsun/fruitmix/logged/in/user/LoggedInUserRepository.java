package com.winsun.fruitmix.logged.in.user;

import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.equipment.search.data.Equipment;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Administrator on 2017/7/6.
 */

public class LoggedInUserRepository extends BaseDataRepository implements LoggedInUserDataSource {

    public static final String TAG = LoggedInUserRepository.class.getSimpleName();

    private static LoggedInUserRepository instance;

    private Collection<LoggedInUser> cacheLoggedInUsers;

    private LoggedInUser currentLoggedInUser;

    private LoggedInUserDBSource loggedInUserDBDataSource;

    private LoggedInUserRemoteDataSource mLoggedInUserRemoteDataSource;

    private boolean loadedAllLoggedInUserFromDB = false;

    private String preQueryUserUUID;

    private LoggedInUserRepository(ThreadManager threadManager, LoggedInUserDBSource loggedInUserDBDataSource, LoggedInUserRemoteDataSource loggedInUserRemoteDataSource) {
        super(threadManager);
        this.loggedInUserDBDataSource = loggedInUserDBDataSource;
        mLoggedInUserRemoteDataSource = loggedInUserRemoteDataSource;

        cacheLoggedInUsers = new ArrayList<>();
    }

    public static LoggedInUserRepository getInstance(ThreadManager threadManager, LoggedInUserDBSource loggedInUserDBDataSource, LoggedInUserRemoteDataSource loggedInUserRemoteDataSource) {

        if (instance == null)
            instance = new LoggedInUserRepository(threadManager, loggedInUserDBDataSource, loggedInUserRemoteDataSource);

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
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

        for (LoggedInUser loggedInUser : loggedInUsers) {

            Iterator<LoggedInUser> iterator = cacheLoggedInUsers.iterator();

            while (iterator.hasNext()) {

                LoggedInUser cacheLoggedInUser = iterator.next();

                if (loggedInUser.getUser().getUuid().equals(cacheLoggedInUser.getUser().getUuid()))
                    iterator.remove();

            }

        }


        return loggedInUserDBDataSource.deleteLoggedInUsers(loggedInUsers);
    }

    @Override
    public boolean clear() {
        return false;
    }

    @Override
    public Collection<LoggedInUser> getAllLoggedInUsers() {

        if (!loadedAllLoggedInUserFromDB) {

            Collection<LoggedInUser> loggedInUsers = loggedInUserDBDataSource.getAllLoggedInUsers();

            cacheLoggedInUsers.clear();
            cacheLoggedInUsers.addAll(loggedInUsers);

            loadedAllLoggedInUserFromDB = true;
        }

        return cacheLoggedInUsers;
    }

    @Override
    public LoggedInUser getLoggedInUserByUserUUID(String userUUID) {

/*        if (currentLoggedInUser == null || currentLoggedInUser.getUser() == null || preQueryUserUUID == null || !preQueryUserUUID.equals(userUUID)) {

            currentLoggedInUser = loggedInUserDBDataSource.getLoggedInUserByUserUUID(userUUID);

            preQueryUserUUID = userUUID;

        }

        return currentLoggedInUser;*/

        return loggedInUserDBDataSource.getLoggedInUserByUserUUID(userUUID);

    }

    @Override
    public LoggedInUser getLoggedInUserByToken(String token) {

        return loggedInUserDBDataSource.getLoggedInUserByToken(token);

    }

    @Override
    public void checkFoundedEquipment(final Equipment foundedEquipment, final LoggedInUser loggedInUser, final BaseOperateDataCallback<Boolean> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mLoggedInUserRemoteDataSource.checkFoundedEquipment(foundedEquipment, loggedInUser, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }
}
