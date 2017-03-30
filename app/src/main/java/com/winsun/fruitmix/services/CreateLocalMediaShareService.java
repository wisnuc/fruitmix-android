package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.MediaShareOperationEvent;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class CreateLocalMediaShareService extends IntentService {

    private static final String TAG = CreateLocalMediaShareService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CREATE_SHARE = "com.winsun.fruitmix.services.action.create.share";

    private static MediaShare mMediaShare;

    public CreateLocalMediaShareService() {
        super("CreateLocalMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionCreateLocalShare(Context context, MediaShare mediaShare) {
        Intent intent = new Intent(context, CreateLocalMediaShareService.class);
        intent.setAction(ACTION_CREATE_SHARE);
        mMediaShare = mediaShare;
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_SHARE.equals(action)) {

                handleActionCreateShare();

                mMediaShare = null;
            }
        }
    }

    /**
     * create local share and start upload share task if network connected
     */
    private void handleActionCreateShare() {

        MediaShare mediaShare = mMediaShare;

        DBUtils dbUtils = DBUtils.getInstance(this);
        long returnValue = dbUtils.insertLocalMediaShare(mediaShare);

        MediaShareOperationEvent mediaShareOperationEvent;

        if (returnValue > 0) {

            mediaShareOperationEvent = new MediaShareOperationEvent(Util.LOCAL_MEDIA_SHARE_CREATED, new OperationSuccess(), mediaShare);

            Log.i(TAG, "insert local mediashare succeed");

            MediaShare mapResult = LocalCache.LocalMediaShareMapKeyIsUUID.put(mediaShare.getUuid(), mediaShare);

            Log.i(TAG, "insert local media share to map result:" + (mapResult != null ? "true" : "false"));

        } else {

            mediaShareOperationEvent = new MediaShareOperationEvent(Util.LOCAL_MEDIA_SHARE_CREATED, new OperationSQLException(), mediaShare);

            Log.i(TAG, "insert local mediashare fail");
        }

        EventBus.getDefault().post(mediaShareOperationEvent);

    }

}
