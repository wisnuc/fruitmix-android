package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ModifyLocalMediaShareService extends IntentService {

    private static final String TAG = ModifyRemoteMediaShareService.class.getSimpleName();

    private static final String ACTION_MODIFY_LOCAL_MEDIA_SHARE = "com.winsun.fruitmix.services.action.modify.local.share";

    // TODO: Rename parameters
    private static final String EXTRA_MEDIA_SHARE = "com.winsun.fruitmix.services.extra.share";

    public ModifyLocalMediaShareService() {
        super("ModifyLocalMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionModifyLocalMediaShare(Context context, MediaShare mediaShare) {
        Intent intent = new Intent(context, ModifyLocalMediaShareService.class);
        intent.setAction(ACTION_MODIFY_LOCAL_MEDIA_SHARE);
        intent.putExtra(EXTRA_MEDIA_SHARE, mediaShare);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MODIFY_LOCAL_MEDIA_SHARE.equals(action)) {
                final MediaShare mediaShare = intent.getParcelableExtra(EXTRA_MEDIA_SHARE);
                handleActionModifyLocalShare(mediaShare);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionModifyLocalShare(MediaShare mediaShare) {

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        Intent intent = new Intent(Util.LOCAL_SHARE_MODIFIED);

        if (!mediaShare.isLocal()) {
            intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.NO_NETWORK.name());
        } else {

            DBUtils dbUtils = DBUtils.getInstance(this);

            long returnValue = dbUtils.updateLocalShare(mediaShare);

            if (returnValue > 0) {

                intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.SUCCEED.name());
                intent.putExtra(Util.OPERATION_MEDIASHARE,mediaShare);

                Log.i(TAG,"modify local share succeed");

            } else {
                intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.FAIL.name());

                Log.i(TAG,"modify local share fail");
            }

        }

        broadcastManager.sendBroadcast(intent);

    }

}

