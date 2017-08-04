package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
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

public class UserDataRepositoryImpl implements UserDataRepository {

    private static UserDataRepositoryImpl instance;

    ConcurrentMap<String, User> cacheUsers;

    private UserDBDataSource userDBDataSource;
    private UserRemoteDataSource userRemoteDataSource;

    boolean cacheDirty = true;

    private UserDataRepositoryImpl(UserDBDataSource userDBDataSource, UserRemoteDataSource userRemoteDataSource) {
        this.userDBDataSource = userDBDataSource;
        this.userRemoteDataSource = userRemoteDataSource;

        cacheUsers = new ConcurrentHashMap<>();
    }

    public static UserDataRepositoryImpl getInstance(UserDBDataSource userDBDataSource, UserRemoteDataSource userRemoteDataSource) {

        if (instance == null)
            instance = new UserDataRepositoryImpl(userDBDataSource, userRemoteDataSource);

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
    public void getUsers(final BaseLoadDataCallback<User> callback) {

        if (!cacheDirty) {
            callback.onSucceed(new ArrayList<>(cacheUsers.values()), new OperationSuccess());

            return;
        }

        userDBDataSource.getUsers(new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                cacheUsers.clear();
                cacheUsers.putAll(buildRemoteUserMapKeyIsUUID(data));

                if (callback != null)
                    callback.onSucceed(data, operationResult);
            }

            @Override
            public void onFail(OperationResult operationResult) {
                if (callback != null)
                    callback.onFail(operationResult);
            }
        });

        userRemoteDataSource.getUsers(new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                userDBDataSource.clearUsers();
                userDBDataSource.insertUser(data);

                cacheUsers.clear();
                cacheUsers.putAll(buildRemoteUserMapKeyIsUUID(data));

                cacheDirty = false;

                if (callback != null)
                    callback.onSucceed(data, operationResult);
            }

            @Override
            public void onFail(OperationResult operationResult) {
                if (callback != null)
                    callback.onFail(operationResult);

                cacheDirty = false;
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
    public void insertUser(String userName, String userPwd, final BaseOperateDataCallback<User> callback) {

        userRemoteDataSource.insertUser(userName, userPwd, new BaseOperateDataCallback<User>() {
            @Override
            public void onSucceed(User data, OperationResult result) {

                userDBDataSource.insertUser(Collections.singletonList(data));

                cacheUsers.put(data.getUuid(), data);

                if (callback != null)
                    callback.onSucceed(data, result);

            }

            @Override
            public void onFail(OperationResult result) {
                if (callback != null)
                    callback.onFail(result);
            }
        });

    }

}
