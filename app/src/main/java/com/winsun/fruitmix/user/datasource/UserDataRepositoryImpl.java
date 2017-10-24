package com.winsun.fruitmix.user.datasource;

import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2017/7/11.
 */

public class UserDataRepositoryImpl extends BaseDataRepository implements UserDataRepository {

    public static final String TAG = UserDataRepositoryImpl.class.getSimpleName();

    private static UserDataRepositoryImpl instance;

    ConcurrentMap<String, User> cacheUsers;

    private UserDBDataSource userDBDataSource;
    private UserRemoteDataSource userRemoteDataSource;

    boolean cacheDirty = true;

    private UserDataRepositoryImpl(UserDBDataSource userDBDataSource, UserRemoteDataSource userRemoteDataSource, ThreadManager threadManager) {
        super(threadManager);
        this.userDBDataSource = userDBDataSource;
        this.userRemoteDataSource = userRemoteDataSource;

        cacheUsers = new ConcurrentHashMap<>();
    }

    public static UserDataRepositoryImpl getInstance(UserDBDataSource userDBDataSource, UserRemoteDataSource userRemoteDataSource, ThreadManager threadManager) {

        if (instance == null)
            instance = new UserDataRepositoryImpl(userDBDataSource, userRemoteDataSource, threadManager);

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    //TODO: when setCacheDirty need call insert user

    @Override
    public void setCacheDirty() {
        cacheDirty = true;
    }

    @Override
    public void getUsers(final String currentLoginUserUUID, final BaseLoadDataCallback<User> callback) {

        final BaseLoadDataCallback<User> runOnMainThreadCallback = createLoadCallbackRunOnMainThread(callback);

        if (!cacheDirty) {

            runOnMainThreadCallback.onSucceed(new ArrayList<>(cacheUsers.values()), new OperationSuccess());

            return;
        }

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                userRemoteDataSource.getUsers(currentLoginUserUUID, new BaseLoadDataCallback<User>() {
                    @Override
                    public void onSucceed(List<User> data, OperationResult operationResult) {

                        if (cacheUsers.isEmpty()) {

                            if (runOnMainThreadCallback != null)
                                runOnMainThreadCallback.onSucceed(data, operationResult);

                        } else {

                            for (User user : data) {

                                User originalUser = cacheUsers.get(user.getUuid());

                                if (originalUser != null) {

                                    user.setHome(originalUser.getHome());

                                    userDBDataSource.updateUser(user);

                                    cacheUsers.put(user.getUuid(), user);
                                } else {

                                    userDBDataSource.insertUser(Collections.singletonList(user));

                                    cacheUsers.putIfAbsent(user.getUuid(), user);
                                }

                            }

                            cacheDirty = false;

                            if (runOnMainThreadCallback != null)
                                runOnMainThreadCallback.onSucceed(new ArrayList<>(cacheUsers.values()), operationResult);

                        }

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

//                        getUserFromDB(runOnMainThreadCallback);

                        if (runOnMainThreadCallback != null)
                            runOnMainThreadCallback.onFail(operationResult);
                    }
                });

            }
        });

    }

    private void getUserFromDB(final BaseLoadDataCallback<User> callback) {

        List<User> users = userDBDataSource.getUsers();

        cacheUsers.clear();
        cacheUsers.putAll(buildRemoteUserMapKeyIsUUID(users));

        cacheDirty = false;

        if (callback != null)
            callback.onSucceed(users, new OperationSuccess());

    }

    private ConcurrentMap<String, User> buildRemoteUserMapKeyIsUUID(Collection<User> users) {

        ConcurrentMap<String, User> userConcurrentMap = new ConcurrentHashMap<>(users.size());
        for (User user : users) {
            userConcurrentMap.put(user.getUuid(), user);
        }
        return userConcurrentMap;

    }

    @Override
    public void insertUser(final String userName, final String userPwd, final BaseOperateDataCallback<User> callback) {

        final BaseOperateDataCallback<User> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                userRemoteDataSource.insertUser(userName, userPwd, new BaseOperateDataCallback<User>() {
                    @Override
                    public void onSucceed(User data, OperationResult result) {

                        userDBDataSource.insertUser(Collections.singletonList(data));

                        cacheUsers.put(data.getUuid(), data);

                        if (runOnMainThreadCallback != null)
                            runOnMainThreadCallback.onSucceed(data, result);

                    }

                    @Override
                    public void onFail(OperationResult result) {
                        if (runOnMainThreadCallback != null)
                            runOnMainThreadCallback.onFail(result);
                    }
                });

            }
        });

    }

    @Override
    public void getCurrentUserHome(final BaseLoadDataCallback<String> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                userRemoteDataSource.getCurrentUserHome(createLoadCallbackRunOnMainThread(callback));
            }
        });

    }


    @Override
    public boolean clearAllUsersInDB() {
        return userDBDataSource.clearUsers();
    }

    @Override
    public void getUsersByStationIDWithCloudAPI(final String stationID, final BaseLoadDataCallback<User> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                userRemoteDataSource.getUsersByStationIDWithCloudAPI(stationID, createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getUserDetailedInfoByUUID(final String userUUID, final BaseLoadDataCallback<User> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                userRemoteDataSource.getUserDetailedInfoByUUID(userUUID, createLoadCallbackRunOnMainThread(callback));
            }
        });

    }


    @Override
    public void insertUsers(Collection<User> users) {

        cacheDirty = false;

        userDBDataSource.clearUsers();
        userDBDataSource.insertUser(users);

        cacheUsers.clear();
        cacheUsers.putAll(buildRemoteUserMapKeyIsUUID(users));

    }

    @Override
    public User getUserByUUID(String userUUID) {

        Log.d(TAG, "getUserByUUID: cacheDirty: " + cacheDirty);

        if (cacheDirty || cacheUsers.isEmpty()) {

            List<User> users = userDBDataSource.getUsers();

            cacheUsers.clear();
            cacheUsers.putAll(buildRemoteUserMapKeyIsUUID(users));

        }

        return cacheUsers.get(userUUID);
    }

    @Override
    public void getWeUserInfoByGUIDWithCloudAPI(final String guid, final BaseLoadDataCallback<User> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                userRemoteDataSource.getWeUserInfoByGUIDWithCloudAPI(guid, createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void modifyUserName(final String userUUID, final String userName, final BaseOperateDataCallback<User> callback) {

        final BaseOperateDataCallback<User> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                userRemoteDataSource.modifyUserName(userUUID, userName, new BaseOperateDataCallback<User>() {
                    @Override
                    public void onSucceed(User data, OperationResult result) {

                        userDBDataSource.modifyUserName(data.getUuid(), data.getUserName());

                        cacheUsers.get(data.getUuid()).setUserName(data.getUserName());

                        runOnMainThreadCallback.onSucceed(data, result);

                    }

                    @Override
                    public void onFail(OperationResult result) {
                        runOnMainThreadCallback.onFail(result);
                    }
                });
            }
        });

    }

    @Override
    public void modifyUserPassword(final String userUUID, final String originalPassword, final String newPassword, final BaseOperateDataCallback<Boolean> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                userRemoteDataSource.modifyUserPassword(userUUID, originalPassword, newPassword, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }
}
