package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.winsun.fruitmix.model.Share;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class DeleteShareService extends IntentService {
    private static final String ACTION_DELETE_SHARE = "com.winsun.fruitmix.services.action.delete.share";

    // TODO: Rename parameters
    private static final String EXTRA_SHARE = "com.winsun.fruitmix.services.extra.share";

    public DeleteShareService() {
        super("DeleteShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDeleteShare(Context context, Share share) {
        Intent intent = new Intent(context, DeleteShareService.class);
        intent.setAction(ACTION_DELETE_SHARE);
        intent.putExtra(EXTRA_SHARE, share);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DELETE_SHARE.equals(action)) {
                final Share share = intent.getParcelableExtra(EXTRA_SHARE);
                handleActionDeleteShare(share);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDeleteShare(Share share) {

    }

}
