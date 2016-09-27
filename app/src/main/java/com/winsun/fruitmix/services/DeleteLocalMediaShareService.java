package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class DeleteLocalMediaShareService extends IntentService {

    private static final String TAG = DeleteLocalMediaShareService.class.getSimpleName();

    private static final String ACTION_DELETE_LOCAL_SHARE = "com.winsun.fruitmix.services.action.delete.local.share";

    private static final String EXTRA_LOCAL_MEDIASHARE_UUID = "com.winsun.fruitmix.services.extra.local_mediashare_uuid";

    private static final String EXTRA_LOCAL_MEDIASHARE_LOCKED = "com.winsun.fruitmix.services.extra.local_mediashare_locked";

    public DeleteLocalMediaShareService() {
        super("DeleteLocalMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDeleteLocalShare(Context context, String localMediaShareUUID,boolean localMediaShareLocked) {
        Intent intent = new Intent(context, DeleteLocalMediaShareService.class);
        intent.setAction(ACTION_DELETE_LOCAL_SHARE);
        intent.putExtra(EXTRA_LOCAL_MEDIASHARE_UUID, localMediaShareUUID);
        intent.putExtra(EXTRA_LOCAL_MEDIASHARE_LOCKED,localMediaShareLocked);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DELETE_LOCAL_SHARE.equals(action)) {
                String localMediaShareUUID = intent.getStringExtra(EXTRA_LOCAL_MEDIASHARE_UUID);
                handleActionDeleteLocalShare(localMediaShareUUID,intent.getBooleanExtra(EXTRA_LOCAL_MEDIASHARE_LOCKED,false));
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDeleteLocalShare(String localMediaShareUUID,boolean localMediaShareLocked) {

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(Util.APPLICATION_CONTEXT);
        Intent intent = new Intent(Util.LOCAL_SHARE_DELETED);

        if (!localMediaShareLocked) {
            intent.putExtra(Util.OPERATION_RESULT, OperationResult.NO_NETWORK.name());
        } else {

            DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

            long value = dbUtils.deleteLocalShareByUUid(localMediaShareUUID);

            if (value > 0) {

                intent.putExtra(Util.OPERATION_RESULT, OperationResult.SUCCEED.name());

                Log.i(TAG, "delete local mediashare succeed");

                MediaShare mapResult = LocalCache.LocalMediaShareMapKeyIsUUID.remove(localMediaShareUUID);

                Log.i(TAG, "delete local media share in map result:" + (mapResult != null ? "true" : "false"));

            } else {
                intent.putExtra(Util.OPERATION_RESULT, OperationResult.FAIL.name());

                Log.i(TAG, "delete local mediashare fail");
            }
        }

        broadcastManager.sendBroadcast(intent);

    }

}
