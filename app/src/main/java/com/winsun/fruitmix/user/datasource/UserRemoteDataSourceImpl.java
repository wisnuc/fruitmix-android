package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.CloudHttpRequestFactory;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.parser.RemoteCurrentUserParser;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteInsertUserParser;
import com.winsun.fruitmix.parser.RemoteUserHomeParser;
import com.winsun.fruitmix.parser.RemoteWeChatUser;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.parser.RemoteLoginUsersParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/7.
 */

public class UserRemoteDataSourceImpl extends BaseRemoteDataSourceImpl implements UserRemoteDataSource {

    public static final String TAG = UserRemoteDataSourceImpl.class.getSimpleName();

    public static final String USER_PARAMETER = "/users";

    public static final String USER_HOME_PARAMETER = "/drives";

    public UserRemoteDataSourceImpl(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);

    }

    /*
     * WISNUC API: POST USER LIST
     * Insert User
     */
    @Override
    public void insertUser(String userName, String userPwd, BaseOperateDataCallback<User> callback) {

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("username", userName);
            jsonObject.put("password", userPwd);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String body = jsonObject.toString();

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(USER_PARAMETER, body);

        wrapper.operateCall(httpRequest, callback, new RemoteInsertUserParser(), R.string.create_user);

    }

    @Override
    public void getUsers(String currentLoginUserUUID, final BaseLoadDataCallback<User> callback) {

        final List<User> users = new ArrayList<>();

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(USER_PARAMETER + "/" + currentLoginUserUUID);

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

    /*
     * WISNUC API:GET USER LIST
     */
    private void getOtherUsers(final List<User> users, final BaseLoadDataCallback<User> callback) {
        HttpRequest loginHttpRequest = httpRequestFactory.createHttpGetRequest(USER_PARAMETER);

        wrapper.loadCall(loginHttpRequest, new BaseLoadDataCallback<User>() {

            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                addDifferentUsers(users, data);

                callback.onSucceed(users, operationResult);
            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);

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


    /*
     * WISNUC API:GET DRIVE LIST
     * Get User Home
     */
    @Override
    public void getCurrentUserHome(BaseLoadDataCallback<String> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(USER_HOME_PARAMETER);

        wrapper.loadCall(httpRequest, callback, new RemoteUserHomeParser());

    }

    @Override
    public void getUsersByStationIDWithCloudAPI(String stationID, BaseLoadDataCallback<User> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequestByCloudAPIWithWrap(USER_PARAMETER, stationID);

        wrapper.loadCall(httpRequest, callback, new RemoteLoginUsersParser());

    }

    /*
     * WISNUC API:GET USER
     * Get User Detailed Info
     */
    @Override
    public void getUserDetailedInfoByUUID(String userUUID, BaseLoadDataCallback<User> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(USER_PARAMETER + "/" + userUUID);

        wrapper.loadCall(httpRequest, callback, new RemoteCurrentUserParser());

    }

    @Override
    public void getWeUserInfoByGUIDWithCloudAPI(String guid, BaseLoadDataCallback<User> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequestByCloudAPIWithoutWrap(CloudHttpRequestFactory.CLOUD_API_LEVEL + "/users/" + guid);

        wrapper.loadCall(httpRequest, callback, new RemoteWeChatUser());

    }

    /*
     * WISNUC API:PATCH USER
     * Modify User Name
     */
    @Override
    public void modifyUserName(String userUUID, String userName, BaseOperateDataCallback<User> callback) {

        String path = USER_PARAMETER + "/" + userUUID;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", userName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequest(path, jsonObject.toString());

        wrapper.operateCall(httpRequest, callback, new RemoteInsertUserParser());

    }

    /*
     * WISNUC API:PUT USER PASSWORD
     * Modify User Password
     */
    @Override
    public void modifyUserPassword(String userUUID, String originalPassword, String newPassword, BaseOperateDataCallback<Boolean> callback) {

        String path = USER_PARAMETER + "/" + userUUID + "/password";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("password", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createModifyPasswordRequest(path, jsonObject.toString(), userUUID, originalPassword);

        wrapper.operateCall(httpRequest, callback, new RemoteDataParser<Boolean>() {
            @Override
            public Boolean parse(String json) throws JSONException {
                return true;
            }
        });

    }

    /*
     * WISNUC API:PATCH USER DISABLED
     * Modify User Enable State
     */
    @Override
    public void modifyUserEnableState(String userUUID, boolean newState, BaseOperateDataCallback<User> callback) {

        String path = USER_PARAMETER + "/" + userUUID;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("disabled", newState);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequest(path, jsonObject.toString());

        wrapper.operateCall(httpRequest, callback, new RemoteInsertUserParser());

    }

    /*
     * WISNUC API: PATCH USER IsAdmin
     * Modify User Is Admin Or Not
     */
    @Override
    public void modifyUserIsAdminState(String userUUID, boolean newIsAdminState, BaseOperateDataCallback<User> callback) {

        String path = USER_PARAMETER + "/" + userUUID;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("isAdmin", newIsAdminState);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequest(path, jsonObject.toString());

        wrapper.operateCall(httpRequest, callback, new RemoteInsertUserParser());

    }

}
