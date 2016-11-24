package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.operationResult.OperationSQLException;
import com.winsun.fruitmix.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;

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
        super("RetrieveLocalMediaService");
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

        List<Map<String, String>> localPhotoList;
        int i;
        Map<String, String> itemRaw;
        Media media;

        int retrieveMediaCount = 0;

        localPhotoList = LocalCache.PhotoList("Camera");

        for (i = 0; i < localPhotoList.size(); i++) {
            itemRaw = localPhotoList.get(i);

            if (LocalCache.LocalMediaMapKeyIsThumb.containsKey(itemRaw.get("thumb"))) {
                continue;
            }
            String uuid = Util.CalcSHA256OfFile(itemRaw.get("thumb"));

            if (LocalCache.LocalMediaMapKeyIsUUID.containsKey(uuid)) {
                continue;
            }

            media = new Media();
            media.setThumb(itemRaw.get("thumb"));
            media.setWidth(itemRaw.get("width"));
            media.setHeight(itemRaw.get("height"));
            media.setTime(itemRaw.get("lastModified"));
            media.setUploaded(false);
            media.setSelected(false);
            media.setLoaded(false);
            media.setOrientationNumber(1);
            media.setLocal(true);
            media.setSharing(true);
            media.setUuid(uuid);
            media.setOrientationNumber(1);

            DBUtils dbUtils = DBUtils.getInstance(this);

            long returnValue = dbUtils.insertLocalMedia(media);

            if (returnValue > 0) {
                Log.i(TAG, "insert local media succeed");

                retrieveMediaCount++;

                Media mapResult = LocalCache.LocalMediaMapKeyIsThumb.put(media.getThumb(), media);

                Log.i(TAG, "insert local media to map key is thumb result:" + (mapResult != null ? "true" : "false"));

                mapResult = LocalCache.LocalMediaMapKeyIsUUID.put(media.getUuid(), media);

                Log.i(TAG, "insert local media to map key is uuid result:" + (mapResult != null ? "true" : "false"));
            }

        }

        OperationEvent operationEvent;
        if (retrieveMediaCount > 0) {
            operationEvent = new OperationEvent(Util.NEW_LOCAL_MEDIA_IN_CAMERA_RETRIEVED, new OperationSuccess());
        } else {
            operationEvent = new OperationEvent(Util.NEW_LOCAL_MEDIA_IN_CAMERA_RETRIEVED, new OperationSQLException());
        }
        EventBus.getDefault().post(operationEvent);
    }

}
