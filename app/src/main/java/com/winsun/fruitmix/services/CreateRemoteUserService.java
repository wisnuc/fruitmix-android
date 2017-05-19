package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteUserJSONObjectParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class CreateRemoteUserService extends IntentService {

    private static final String TAG = CreateRemoteUserService.class.getSimpleName();

    private static final String ACTION_CREATE_REMOTE_USER = "com.winsun.fruitmix.services.action.create.remote.user";

    private static final String EXTRA_USER_NAME = "com.winsun.fruitmix.services.extra.USER_NAME";
    private static final String EXTRA_USER_PASSWORD = "com.winsun.fruitmix.services.extra.USER_PASSWORD";

    public CreateRemoteUserService() {
        super("CreateRemoteUserService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionCreateRemoteUser(Context context, String userName, String userPassword) {
        Intent intent = new Intent(context, CreateRemoteUserService.class);
        intent.setAction(ACTION_CREATE_REMOTE_USER);
        intent.putExtra(EXTRA_USER_NAME, userName);
        intent.putExtra(EXTRA_USER_PASSWORD, userPassword);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_REMOTE_USER.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_USER_NAME);
                final String param2 = intent.getStringExtra(EXTRA_USER_PASSWORD);
                handleActionCreateRemoteUser(param1, param2);
            }
        }
    }

    private void handleActionCreateRemoteUser(String userName, String userPassword) {

        String body = User.generateCreateRemoteUserBody(userName, userPassword);

        HttpResponse httpResponse;

        OperationEvent operationEvent;

        try {
            httpResponse = FNAS.PostRemoteCall(this,Util.ADMIN_USER_PARAMETER, body);

            if (httpResponse.getResponseCode() == 200) {

                User user = new RemoteUserJSONObjectParser().getUser(new JSONObject(httpResponse.getResponseData()));

                DBUtils dbUtils = DBUtils.getInstance(this);
                long dbResult = dbUtils.insertRemoteUser(user);

                Log.i(TAG, "insert remote user which source is db result:" + dbResult);

                User mapResult = LocalCache.RemoteUserMapKeyIsUUID.put(user.getUuid(), user);

                Log.i(TAG, "insert remote user to map result:" + (mapResult != null ? "true" : "false"));

                operationEvent = new OperationEvent(Util.REMOTE_USER_CREATED, new OperationSuccess(R.string.create_user));

            } else {

                operationEvent = new OperationEvent(Util.REMOTE_USER_CREATED, new OperationNetworkException(httpResponse.getResponseCode()));

                Log.i(TAG, "insert remote user fail");

            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();

            operationEvent = new OperationEvent(Util.REMOTE_USER_CREATED, new OperationMalformedUrlException());

            Log.i(TAG, "insert remote user fail");

        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();

            operationEvent = new OperationEvent(Util.REMOTE_USER_CREATED, new OperationSocketTimeoutException());

            Log.i(TAG, "insert remote user fail");

        } catch (IOException e) {
            e.printStackTrace();

            operationEvent = new OperationEvent(Util.REMOTE_USER_CREATED, new OperationIOException());

            Log.i(TAG, "insert remote user fail");

        } catch (JSONException e) {
            e.printStackTrace();

            operationEvent = new OperationEvent(Util.REMOTE_USER_CREATED, new OperationJSONException());

            Log.i(TAG, "insert remote user fail");
        }

        EventBus.getDefault().post(operationEvent);

    }

}
