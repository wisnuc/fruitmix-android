package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.MediaShareOperationEvent;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.operationResult.OperationNoNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ModifyLocalMediaShareService extends IntentService {

    private static final String TAG = ModifyRemoteMediaShareService.class.getSimpleName();

    private static final String ACTION_MODIFY_LOCAL_MEDIA_SHARE = "com.winsun.fruitmix.services.action.modify.local.share";

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

        mediaShare.clearMediaShareContents();

        intent.putExtra(EXTRA_MEDIA_SHARE, mediaShare);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MODIFY_LOCAL_MEDIA_SHARE.equals(action)) {
                MediaShare mediaShare = intent.getParcelableExtra(EXTRA_MEDIA_SHARE);
                handleActionModifyLocalShare(mediaShare);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionModifyLocalShare(MediaShare mediaShare) {

        MediaShareOperationEvent mediaShareOperationEvent;

        if (!mediaShare.isLocal()) {

            mediaShareOperationEvent = new MediaShareOperationEvent(Util.LOCAL_SHARE_MODIFIED, new OperationNoNetworkException(), mediaShare);

        } else {

            DBUtils dbUtils = DBUtils.getInstance(this);

            long returnValue = dbUtils.updateLocalMediaShare(mediaShare);

            if (returnValue > 0) {

                mediaShareOperationEvent = new MediaShareOperationEvent(Util.LOCAL_SHARE_MODIFIED, new OperationSuccess(), mediaShare);

                Log.i(TAG, "modify local share succeed");

            } else {

                mediaShareOperationEvent = new MediaShareOperationEvent(Util.LOCAL_SHARE_MODIFIED, new OperationSQLException(), mediaShare);

                Log.i(TAG, "modify local share fail");
            }

        }

        EventBus.getDefault().post(mediaShareOperationEvent);

    }

}

