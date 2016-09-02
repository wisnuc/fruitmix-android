package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.winsun.fruitmix.model.MediaShare;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ModifyRemoteMediaShareService extends IntentService {

    private static final String ACTION_MODIFY_REMOTE_MEDIA_SHARE = "com.winsun.fruitmix.services.action.modify.remote.share";

    // TODO: Rename parameters
    private static final String EXTRA_MEDIA_SHARE = "com.winsun.fruitmix.services.extra.share";

    public ModifyRemoteMediaShareService() {
        super("ModifyRemoteMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionModifyRemoteMediaShare(Context context, MediaShare mediaShare) {
        Intent intent = new Intent(context, ModifyRemoteMediaShareService.class);
        intent.setAction(ACTION_MODIFY_REMOTE_MEDIA_SHARE);
        intent.putExtra(EXTRA_MEDIA_SHARE, mediaShare);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MODIFY_REMOTE_MEDIA_SHARE.equals(action)) {
                final MediaShare mediaShare = intent.getParcelableExtra(EXTRA_MEDIA_SHARE);
                handleActionModifyRemoteShare(mediaShare);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionModifyRemoteShare(MediaShare mediaShare) {

    }

}
