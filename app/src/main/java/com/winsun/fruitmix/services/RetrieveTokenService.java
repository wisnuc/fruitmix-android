package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

import org.json.JSONObject;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RetrieveTokenService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RETRIEVE_TOKEN = "com.winsun.fruitmix.services.action.retrieve.token";

    private static String EXTRA_GATEWAY = "com.winsun.fruitmix.services.gateway";

    private static String EXTRA_USER_UUID = "com.winsun.fruitmix.services.user.uuid";

    private static String EXTRA_USER_PASSWORD = "com.winsun.fruitmix.services.user.password";

    public RetrieveTokenService() {
        super("RetrieveTokenService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRetrieveToken(Context context, String gateway, String userUUID, String userPassword) {
        Intent intent = new Intent(context, RetrieveTokenService.class);
        intent.setAction(ACTION_RETRIEVE_TOKEN);
        intent.putExtra(EXTRA_GATEWAY, gateway);
        intent.putExtra(EXTRA_USER_UUID, userUUID);
        intent.putExtra(EXTRA_USER_PASSWORD, userPassword);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RETRIEVE_TOKEN.equals(action)) {
                String gateway = intent.getStringExtra(EXTRA_GATEWAY);
                String userUUID = intent.getStringExtra(EXTRA_USER_UUID);
                String userPassword = intent.getStringExtra(EXTRA_USER_PASSWORD);
                handleActionRetrieveToken(gateway, userUUID, userPassword);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveToken(String gateway, String userUUID, String userPassword) {

        String str;

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(Util.REMOTE_TOKEN_RETRIEVED);

        try {

            str = FNAS.loadToken(this, gateway, userUUID, userPassword);

            if (str.length() > 0) {
                FNAS.JWT = new JSONObject(str).getString("token");
            }

            intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.SUCCEED.name());

        } catch (Exception e) {
            e.printStackTrace();

            intent.putExtra(Util.OPERATION_RESULT_NAME,OperationResult.FAIL.name());
        }

        localBroadcastManager.sendBroadcast(intent);

    }
}