package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteDatasParser;
import com.winsun.fruitmix.parser.RemoteUserJSONObjectParser;
import com.winsun.fruitmix.parser.RemoteLoginUsersParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RetrieveRemoteUserService extends IntentService {

    public static final String TAG = RetrieveRemoteUserService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RETRIEVE_REMOET_USER = "com.winsun.fruitmix.services.action.retrieve_remote_user";

    public RetrieveRemoteUserService() {
        super("RetrieveRemoteUserService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRetrieveRemoteUser(Context context) {
        Intent intent = new Intent(context, RetrieveRemoteUserService.class);
        intent.setAction(ACTION_RETRIEVE_REMOET_USER);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RETRIEVE_REMOET_USER.equals(action)) {
                handleActionRetrieveRemoteUser();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveRemoteUser() {

        List<User> users;

        ConcurrentMap<String, User> userConcurrentMap;
        DBUtils dbUtils = DBUtils.getInstance(this);

        HttpResponse httpResponse;

        if (Util.loginType != LoginType.LOGIN) {

            users = dbUtils.getAllRemoteUser();

            userConcurrentMap = LocalCache.BuildRemoteUserMapKeyIsUUID(users);

            Log.i(TAG, "handleActionRetrieveRemoteUser: retrieve user from db");

            fillUserMap(userConcurrentMap);

            sendEvent(new OperationSuccess(R.string.operate));

        }

        try {

            httpResponse = FNAS.loadUser(this);

            if (httpResponse.getResponseCode() != 200 && Util.loginType == LoginType.LOGIN) {
                OperationEvent operationEvent = new OperationEvent(Util.REMOTE_USER_RETRIEVED, new OperationNetworkException(httpResponse.getResponseCode()));
                EventBus.getDefault().post(operationEvent);
                return;
            }

            User user = new RemoteUserJSONObjectParser().getUser(new JSONObject(httpResponse.getResponseData()));

            users = new ArrayList<>();
            users.add(user);

            LocalCache.saveUser(this, user.getUserName(), user.getDefaultAvatarBgColor(), user.isAdmin(), user.getHome(), user.getUuid());

            if (LocalCache.DeviceID == null || LocalCache.DeviceID.isEmpty()) {
                LocalCache.DeviceID = user.getLibrary();
                LocalCache.setGlobalData(this, Util.DEVICE_ID_MAP_NAME, LocalCache.DeviceID);
                Log.d(TAG, "deviceID: " + LocalCache.getGlobalData(this, Util.DEVICE_ID_MAP_NAME));
            }

            RemoteDatasParser<User> parser = new RemoteLoginUsersParser();
            List<User> otherUsers = parser.parse(FNAS.loadOtherUsers(this).getResponseData());

            addDifferentUsers(users, otherUsers);

            userConcurrentMap = LocalCache.BuildRemoteUserMapKeyIsUUID(users);

            dbUtils.deleteAllRemoteUser();
            dbUtils.insertRemoteUsers(userConcurrentMap.values());

            Log.i(TAG, "handleActionRetrieveRemoteUser: retrieve user from network");

            fillUserMap(userConcurrentMap);

            sendEvent(new OperationSuccess(R.string.operate));

        } catch (Exception e) {
            e.printStackTrace();

            if (Util.clearFileDownloadItem)
                Util.clearFileDownloadItem = false;

            if (Util.loginType == LoginType.LOGIN)
                sendEvent(new OperationIOException());
        }

    }

    private void fillUserMap(ConcurrentMap<String, User> userConcurrentMap) {
        LocalCache.RemoteUserMapKeyIsUUID.clear();

        LocalCache.RemoteUserMapKeyIsUUID.putAll(userConcurrentMap);
    }

    private void sendEvent(OperationResult operationResult) {
        OperationEvent operationEvent = new OperationEvent(Util.REMOTE_USER_RETRIEVED, operationResult);
        EventBus.getDefault().post(operationEvent);
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
