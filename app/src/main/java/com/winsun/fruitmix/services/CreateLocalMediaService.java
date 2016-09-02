package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.Media;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class CreateLocalMediaService extends IntentService {

    private static final String ACTION_CREATE_LOCAL_MEDIA = "com.winsun.fruitmix.services.action.create.local.media";

    private static final String EXTRA_MEDIAS = "com.winsun.fruitmix.services.extra.medias";

    public CreateLocalMediaService() {
        super("CreateLocalMediaService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionCreateLocalMedia(Context context, ArrayList<Media> medias) {
        Intent intent = new Intent(context, CreateLocalMediaService.class);
        intent.setAction(ACTION_CREATE_LOCAL_MEDIA);
        intent.putParcelableArrayListExtra(EXTRA_MEDIAS, medias);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_LOCAL_MEDIA.equals(action)) {

                final List<Media> medias = intent.getParcelableArrayListExtra(EXTRA_MEDIAS);
                handleActionCreateLocalMedia(medias);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCreateLocalMedia(List<Media> medias) {

    }

}
