package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2017/7/11.
 */

public class UserDataRepositoryImpl extends BaseDataRepository implements UserDataRepository {

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

    @Override
    public void setCacheDirty() {
        cacheDirty = true;
    }

    @Override
    public void getUsers(final String currentLoginUserUUID,final BaseLoadDataCallback<User> callback) {

        final BaseLoadDataCallback<User> runOnMainThreadCallback = createLoadCallbackRunOnMainThread(callback);

        if (!cacheDirty) {

            runOnMainThreadCallback.onSucceed(new ArrayList<>(cacheUsers.values()), new OperationSuccess());

            return;
        }

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                userRemoteDataSource.getUsers(currentLoginUserUUID,new BaseLoadDataCallback<User>() {
                    @Override
                    public void onSucceed(List<User> data, OperationResult operationResult) {

                        userDBDataSource.clearUsers();
                        userDBDataSource.insertUser(data);

                        cacheUsers.clear();
                        cacheUsers.putAll(buildRemoteUserMapKeyIsUUID(data));

                        cacheDirty = false;

                        if (runOnMainThreadCallback != null)
                            runOnMainThreadCallback.onSucceed(data, operationResult);
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        getUserFromDB(runOnMainThreadCallback);
                    }
                });

            }
        });

    }

    private void getUserFromDB(final BaseLoadDataCallback<User> callback) {
        userDBDataSource.getUsers(new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                cacheUsers.clear();
                cacheUsers.putAll(buildRemoteUserMapKeyIsUUID(data));

                cacheDirty = false;

                if (callback != null)
                    callback.onSucceed(data, operationResult);
            }

            @Override
            public void onFail(OperationResult operationResult) {

                cacheDirty = false;

                if (callback != null)
                    callback.onFail(operationResult);
            }
        });
    }

    private ConcurrentMap<String, User> buildRemoteUserMapKeyIsUUID(List<User> users) {

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
    public void getUsersByStationID(final String stationID, final BaseLoadDataCallback<User> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                userRemoteDataSource.getUsersByStationID(stationID,createLoadCallbackRunOnMainThread(callback));
            }
        });


    }

    @Override
    public User getUserByUUID(String userUUID) {
        return cacheUsers.get(userUUID);
    }
}
