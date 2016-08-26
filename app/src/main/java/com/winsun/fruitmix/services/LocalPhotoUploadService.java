package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class LocalPhotoUploadService extends IntentService {

    private static final String TAG = LocalPhotoUploadService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_UPLOAD_LOCAL_PHOTO = "com.winsun.fruitmix.services.action.upload.local.photo";

    private boolean uploadResult = false;
    private boolean isSave = false;
    private LocalBroadcastManager mManager;

    private static boolean isStop = false;

    public LocalPhotoUploadService() {
        super("LocalPhotoUploadService");
    }

    /**
     * Starts this service to perform action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionUploadLocalPhoto(Context context) {
        Intent intent = new Intent(context, LocalPhotoUploadService.class);
        intent.setAction(ACTION_UPLOAD_LOCAL_PHOTO);
        context.startService(intent);
    }

    public static void stopActionUploadLocalPhoto() {
        isStop = true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD_LOCAL_PHOTO.equals(action)) {
                handleActionUploadLocalPhoto();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUploadLocalPhoto() {
        // TODO: Handle action upload local photo
        mManager = LocalBroadcastManager.getInstance(this.getApplicationContext());

        if (!Util.getNetworkState(Util.APPLICATION_CONTEXT)) {
            return;
        }

        boolean result;
        uploadResult = false;
        for (ConcurrentMap<String, String> map : LocalCache.LocalImagesMap.values()) {

            if (isStop) {
                uploadResult = false;
                isStop = false;
                break;
            }

            if (!map.containsKey(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS) || map.get(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS).equals("false")) {
                result = FNAS.UploadFile(map.get("thumb"));
                map.put(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS, String.valueOf(result));

                Log.i(TAG, "upload file:" + map.get("thumb") + "result:" + result);
                if (result)
                    uploadResult = result;
            }
        }

        if (uploadResult) {
            LocalCache.SetGlobalHashMap(Util.LOCAL_IMAGE_MAP_NAME, LocalCache.LocalImagesMap);
            Intent intent = new Intent(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED);
            mManager.sendBroadcast(intent);
            isSave = true;

            uploadResult = false;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //save upload state if error happened
        if (uploadResult && !isSave) {

            Log.i(TAG, "onDestroy set upload photo");

            LocalCache.SetGlobalHashMap(Util.LOCAL_IMAGE_MAP_NAME, LocalCache.LocalImagesMap);

            Intent intent = new Intent(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED);
            mManager.sendBroadcast(intent);
        }

    }
}
