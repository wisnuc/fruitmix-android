package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.winsun.fruitmix.model.MediaShare;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class DeleteRemoteMediaShareService extends IntentService {
    private static final String ACTION_DELETE_REMOTE_SHARE = "com.winsun.fruitmix.services.action.delete.remote.share";

    // TODO: Rename parameters
    private static final String EXTRA_SHARE = "com.winsun.fruitmix.services.extra.share";

    public DeleteRemoteMediaShareService() {
        super("DeleteRemoteMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDeleteRemoteShare(Context context, MediaShare mediaShare) {
        Intent intent = new Intent(context, DeleteRemoteMediaShareService.class);
        intent.setAction(ACTION_DELETE_REMOTE_SHARE);
        intent.putExtra(EXTRA_SHARE, mediaShare);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DELETE_REMOTE_SHARE.equals(action)) {
                final MediaShare mediaShare = intent.getParcelableExtra(EXTRA_SHARE);
                handleActionDeleteRemoteShare(mediaShare);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDeleteRemoteShare(MediaShare mediaShare) {

    }

}
