package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
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

        LocalCache.DeviceID = LocalCache.getGlobalData(this,Util.DEVICE_ID_MAP_NAME);
        if (LocalCache.DeviceID != null && !LocalCache.DeviceID.equals("")) {

            operationEvent = new OperationEvent(Util.REMOTE_DEVICEID_RETRIEVED, new OperationSuccess(R.string.operate));

        } else {

            try {
                HttpResponse httpResponse = FNAS.loadDeviceId();

                if (httpResponse.getResponseCode() == 200) {

                    LocalCache.DeviceID = new JSONObject(httpResponse.getResponseData()).getString("uuid");

                    LocalCache.setGlobalData(this,Util.DEVICE_ID_MAP_NAME, LocalCache.DeviceID);
                    Log.d(TAG, "deviceID: " + LocalCache.getGlobalData(this,Util.DEVICE_ID_MAP_NAME));

                    operationEvent = new OperationEvent(Util.REMOTE_DEVICEID_RETRIEVED, new OperationSuccess(R.string.operate));

                } else {

                    operationEvent = new OperationEvent(Util.REMOTE_DEVICEID_RETRIEVED, new OperationNetworkException(httpResponse.getResponseCode()));
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();

                operationEvent = new OperationEvent(Util.REMOTE_DEVICEID_RETRIEVED, new OperationMalformedUrlException());
            } catch (SocketTimeoutException e) {
                e.printStackTrace();

                operationEvent = new OperationEvent(Util.REMOTE_DEVICEID_RETRIEVED, new OperationSocketTimeoutException());
            } catch (IOException e) {
                e.printStackTrace();

                operationEvent = new OperationEvent(Util.REMOTE_DEVICEID_RETRIEVED, new OperationIOException());
            } catch (JSONException e) {
                e.printStackTrace();

                operationEvent = new OperationEvent(Util.REMOTE_DEVICEID_RETRIEVED, new OperationJSONException());
            }

        }

        EventBus.getDefault().post(operationEvent);

    }

}
