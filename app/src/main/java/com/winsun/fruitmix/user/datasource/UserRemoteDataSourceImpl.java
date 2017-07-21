package com.winsun.fruitmix.user.datasource;

import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteDatasParser;
import com.winsun.fruitmix.parser.RemoteUserJSONObjectParser;
import com.winsun.fruitmix.parser.RemoteUserParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
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

        HttpResponse httpResponse;

        try {

            HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(ADMIN_USER_PARAMETER, body);

            httpResponse = iHttpUtil.remoteCall(httpRequest);

            if (httpResponse.getResponseCode() == 200) {

                User user = new RemoteUserJSONObjectParser().getUser(new JSONObject(httpResponse.getResponseData()));

                callback.onSucceed(user, new OperationSuccess(R.string.create_user));

            } else {

                callback.onFail(new OperationNetworkException(httpResponse.getResponseCode()));

                Log.i(TAG, "insert remote user fail");

            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();

            callback.onFail(new OperationMalformedUrlException());

            Log.i(TAG, "insert remote user fail");

        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();

            callback.onFail(new OperationSocketTimeoutException());

            Log.i(TAG, "insert remote user fail");

        } catch (IOException e) {
            e.printStackTrace();

            callback.onFail(new OperationIOException());

            Log.i(TAG, "insert remote user fail");

        } catch (JSONException e) {
            e.printStackTrace();

            callback.onFail(new OperationJSONException());

            Log.i(TAG, "insert remote user fail");
        }

    }

    @Override
    public void getUsers(BaseLoadDataCallback<User> callback) {

        List<User> users;

        HttpResponse httpResponse;

        try {

            HttpRequest httpRequest = new HttpRequest(FNAS.generateUrl(ACCOUNT_PARAMETER), Util.HTTP_GET_METHOD);
            httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);

            httpResponse = iHttpUtil.remoteCall(httpRequest);

            if (httpResponse.getResponseCode() != 200 && Util.loginType == LoginType.LOGIN) {
                OperationEvent operationEvent = new OperationEvent(Util.REMOTE_USER_RETRIEVED, new OperationNetworkException(httpResponse.getResponseCode()));
                EventBus.getDefault().post(operationEvent);
                return;
            }

            User user = new RemoteUserJSONObjectParser().getUser(new JSONObject(httpResponse.getResponseData()));

            users = new ArrayList<>();
            users.add(user);

            RemoteDatasParser<User> parser = new RemoteUserParser();

            httpRequest.setUrl(FNAS.generateUrl(LOGIN_PARAMETER));

            List<User> otherUsers = parser.parse(iHttpUtil.remoteCall(httpRequest).getResponseData());

            addDifferentUsers(users, otherUsers);

            callback.onSucceed(users, new OperationSuccess());

        } catch (MalformedURLException ex) {
            ex.printStackTrace();

            callback.onFail(new OperationMalformedUrlException());

            Log.i(TAG, "insert remote user fail");

        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();

            callback.onFail(new OperationSocketTimeoutException());

            Log.i(TAG, "insert remote user fail");

        } catch (IOException e) {
            e.printStackTrace();

            callback.onFail(new OperationIOException());

            Log.i(TAG, "insert remote user fail");

        } catch (JSONException e) {
            e.printStackTrace();

            callback.onFail(new OperationJSONException());

            Log.i(TAG, "insert remote user fail");
        }

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
