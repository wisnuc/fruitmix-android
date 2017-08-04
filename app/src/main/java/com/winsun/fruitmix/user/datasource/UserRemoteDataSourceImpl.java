package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.parser.RemoteCurrentUserParser;
import com.winsun.fruitmix.parser.RemoteInsertUserParser;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteLoginUsersParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/7.
 */

public class UserRemoteDataSourceImpl extends BaseRemoteDataSourceImpl implements UserRemoteDataSource {

    public static final String TAG = UserRemoteDataSourceImpl.class.getSimpleName();

    public static final String ADMIN_USER_PARAMETER = "/admin/users";
    public static final String ACCOUNT_PARAMETER = "/account";
    public static final String LOGIN_PARAMETER = "/users";

    public UserRemoteDataSourceImpl(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void insertUser(String userName, String userPwd, BaseOperateDataCallback<User> callback) {

        String body = User.generateCreateRemoteUserBody(userName, userPwd);

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(ADMIN_USER_PARAMETER, body);

        wrapper.operateCall(httpRequest, callback, new RemoteInsertUserParser(), R.string.create_user);

    }

    @Override
    public void getUsers(final BaseLoadDataCallback<User> callback) {

        final List<User> users = new ArrayList<>();

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(ACCOUNT_PARAMETER);

        wrapper.loadCall(httpRequest, new BaseLoadDataCallback<User>() {

            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                users.addAll(data);

                getOtherUsers(users, callback);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                getOtherUsers(users, callback);

            }
        }, new RemoteCurrentUserParser());

    }

    private void getOtherUsers(final List<User> users, final BaseLoadDataCallback<User> callback) {
        HttpRequest loginHttpRequest = httpRequestFactory.createGetRequestByPathWithoutToken(LOGIN_PARAMETER);

        wrapper.loadCall(loginHttpRequest, new BaseLoadDataCallback<User>() {

            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                addDifferentUsers(users, data);

                callback.onSucceed(users, operationResult);
            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onSucceed(users, new OperationSuccess());

            }
        }, new RemoteLoginUsersParser());
    }


    private void addDifferentUsers(List<User> users, List<User> otherUsers) {
        for (User otherUser : otherUsers) {
            int i;
            for (i = 0; i < users.size(); i++) {
                if (otherUser.getUuid().equals(users.get(i).getUuid())) {
                    break;
                }
            }
            if (i >= users.size()) {
                users.add(otherUser);
            }
        }
    }
}
