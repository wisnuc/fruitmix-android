package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class LocalPhotoUploadService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_UPLOAD_LOCAL_PHOTO = "com.winsun.fruitmix.services.action.upload.local.photo";


    public LocalPhotoUploadService() {
        super("LocalPhotoUploadService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
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
        try {
            FNAS.UploadAll();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        LocalCache.SetGlobalHashMap("localImagesMap", LocalCache.LocalImagesMap);
    }

}
