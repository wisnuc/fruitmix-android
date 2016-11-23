package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RetrieveDeviceIdService extends IntentService {

    public static final String TAG = RetrieveDeviceIdService.class.getSimpleName();

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
                handleActionRetrieveDeviceId();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveDeviceId() {

        OperationEvent operationEvent;

        LocalCache.DeviceID = LocalCache.GetGlobalData(Util.DEVICE_ID_MAP_NAME);
        if(LocalCache.DeviceID != null && !LocalCache.DeviceID.equals("")){

            operationEvent = new OperationEvent(Util.REMOTE_DEVICEID_RETRIEVED, OperationResultType.SUCCEED);

        }else {

            try {
                String str = FNAS.loadDeviceId();

                if (str.length() > 0) {
                    LocalCache.DeviceID = new JSONObject(str).getString("uuid");
                }

                LocalCache.SetGlobalData(Util.DEVICE_ID_MAP_NAME, LocalCache.DeviceID);
                Log.d(TAG, "deviceID: " + LocalCache.GetGlobalData(Util.DEVICE_ID_MAP_NAME));

                operationEvent = new OperationEvent(Util.REMOTE_DEVICEID_RETRIEVED, OperationResultType.SUCCEED);

            } catch (Exception e) {
                e.printStackTrace();

                operationEvent = new OperationEvent(Util.REMOTE_DEVICEID_RETRIEVED, OperationResultType.FAIL);
            }
        }

        EventBus.getDefault().postSticky(operationEvent);

    }

}
