package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RetrieveDeviceIdService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RetrieveDeviceId = "com.winsun.fruitmix.services.action.retrieve.deviceId";


    public RetrieveDeviceIdService() {
        super("RetrieveDeviceIdService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRetrieveDeviceId(Context context) {
        Intent intent = new Intent(context, RetrieveDeviceIdService.class);
        intent.setAction(ACTION_RetrieveDeviceId);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RetrieveDeviceId.equals(action)) {
                handleActionRetrieveDevicedId();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveDevicedId() {


        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(Util.REMOTE_DEVICEID_RETRIEVED);

        try {
            FNAS.loadDeviceId();

            intent.putExtra(Util.OPERATION_RESULT, OperationResult.SUCCEED.name());

        } catch (Exception e) {
            e.printStackTrace();

            intent.putExtra(Util.OPERATION_RESULT,OperationResult.FAIL.name());
        }

        localBroadcastManager.sendBroadcast(intent);
    }

}
