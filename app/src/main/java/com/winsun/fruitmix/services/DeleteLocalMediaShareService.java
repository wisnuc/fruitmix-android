package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class DeleteLocalMediaShareService extends IntentService {

    private static final String TAG = DeleteLocalMediaShareService.class.getSimpleName();

    private static final String ACTION_DELETE_LOCAL_SHARE = "com.winsun.fruitmix.services.action.delete.local.share";

    private static final String EXTRA_LOCAL_MEDIASHARE_UUID = "com.winsun.fruitmix.services.extra.local_mediashare_uuid";

    private static final String EXTRA_LOCAL_MEDIASHARE_LOCKED = "com.winsun.fruitmix.services.extra.local_mediashare_locked";

    private static final String EXTRA_MEDIASHARE = "com.winsun.fruitmix.services.extra.mediashare";

    public DeleteLocalMediaShareService() {
        super("DeleteLocalMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDeleteLocalShare(Context context, MediaShare mediaShare) {
        Intent intent = new Intent(context, DeleteLocalMediaShareService.class);
        intent.setAction(ACTION_DELETE_LOCAL_SHARE);
        intent.putExtra(EXTRA_MEDIASHARE,mediaShare);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DELETE_LOCAL_SHARE.equals(action)) {
                MediaShare mediaShare = intent.getParcelableExtra(EXTRA_MEDIASHARE);
                handleActionDeleteLocalShare(mediaShare);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDeleteLocalShare(MediaShare mediaShare) {

        OperationEvent operationEvent;

        if (!mediaShare.isLocal()) {

            operationEvent = new OperationEvent(Util.LOCAL_SHARE_DELETED,OperationResult.NO_NETWORK);

        } else {

            DBUtils dbUtils = DBUtils.getInstance(this);

            long value = dbUtils.deleteLocalShareByUUid(mediaShare.getUuid());

            if (value > 0) {

                operationEvent = new OperationEvent(Util.LOCAL_SHARE_DELETED,OperationResult.SUCCEED);

                Log.i(TAG, "delete local mediashare succeed");

                MediaShare mapResult = LocalCache.LocalMediaShareMapKeyIsUUID.remove(mediaShare.getUuid());

                Log.i(TAG, "delete local media share in map result:" + (mapResult != null ? "true" : "false"));

            } else {

                operationEvent = new OperationEvent(Util.LOCAL_SHARE_DELETED,OperationResult.FAIL);

                Log.i(TAG, "delete local mediashare fail");
            }
        }

        EventBus.getDefault().post(operationEvent);

    }

}
