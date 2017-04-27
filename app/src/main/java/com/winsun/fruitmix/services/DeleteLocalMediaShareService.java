package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
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
public class DeleteLocalMediaShareService extends IntentService {

    private static final String TAG = DeleteLocalMediaShareService.class.getSimpleName();

    private static final String ACTION_DELETE_LOCAL_SHARE = "com.winsun.fruitmix.services.action.delete.local.share";

    private static final String EXTRA_LOCAL_MEDIA_SHARE_UUID = "com.winsun.fruitmix.services.extra.local_media_share_uuid";

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
        intent.putExtra(EXTRA_LOCAL_MEDIA_SHARE_UUID, mediaShare.getUuid());
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DELETE_LOCAL_SHARE.equals(action)) {
                String mediaShareUUID = intent.getStringExtra(EXTRA_LOCAL_MEDIA_SHARE_UUID);

                handleActionDeleteLocalShare(LocalCache.findMediaShareInLocalCacheMap(mediaShareUUID));
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

            operationEvent = new OperationEvent(Util.LOCAL_MEDIA_SHARE_DELETED, new OperationNoNetworkException());

        } else {

            DBUtils dbUtils = DBUtils.getInstance(this);

            long value = dbUtils.deleteLocalShareByUUIDs(new String[]{mediaShare.getUuid()});

            if (value > 0) {

                operationEvent = new OperationEvent(Util.LOCAL_MEDIA_SHARE_DELETED, new OperationSuccess(R.string.operate));

                Log.i(TAG, "delete local mediashare succeed");

                MediaShare mapResult = LocalCache.LocalMediaShareMapKeyIsUUID.remove(mediaShare.getUuid());

                Log.i(TAG, "delete local media share in map result:" + (mapResult != null ? "true" : "false"));

            } else {

                operationEvent = new OperationEvent(Util.LOCAL_MEDIA_SHARE_DELETED, new OperationSQLException());

                Log.i(TAG, "delete local mediashare fail");
            }
        }

        EventBus.getDefault().post(operationEvent);

    }

}
