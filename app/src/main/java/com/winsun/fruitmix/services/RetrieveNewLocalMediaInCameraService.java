package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RetrieveNewLocalMediaInCameraService extends IntentService {

    private static final String TAG = RetrieveNewLocalMediaInCameraService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CREATE_LOCAL_MEDIA = "com.winsun.fruitmix.services.action.retriev.local.media";

    public RetrieveNewLocalMediaInCameraService() {
        super("RetrieveNewLocalMediaInCameraService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRetrieveNewLocalMediaInCamera(Context context) {
        Intent intent = new Intent(context, RetrieveNewLocalMediaInCameraService.class);
        intent.setAction(ACTION_CREATE_LOCAL_MEDIA);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_LOCAL_MEDIA.equals(action)) {
                handleActionRetrieveLocalMedia();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveLocalMedia() {

        Log.i(TAG, "handleActionRetrieveLocalMedia: before retrieve local media time:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        List<Media> medias = LocalCache.PhotoList(this, "Camera");

        Log.i(TAG, "handleActionRetrieveLocalMedia: after retrieve local media time:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        if (medias.size() != 0) {

            CalcNewLocalMediaDigestService.startActionCalcNewLocalMediaDigest(this);

            Log.i(TAG, "handleActionRetrieveLocalMedia: media size:" + medias.size());

        }
    }

}
