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
public class CreateLocalMediaShareService extends IntentService {

    private static final String TAG = CreateLocalMediaShareService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CREATE_SHARE = "com.winsun.fruitmix.services.action.create.share";

    // TODO: Rename parameters
    private static final String EXTRA_SHARE = "com.winsun.fruitmix.services.extra.share";

    public CreateLocalMediaShareService() {
        super("CreateShareService");
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
        intent.putExtra(EXTRA_SHARE, mediaShare);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_SHARE.equals(action)) {
                final MediaShare mediaShare = intent.getParcelableExtra(EXTRA_SHARE);
                handleActionCreateShare(mediaShare);
            }
        }
    }

    /**
     * create local share and start upload share task if network connected
     */
    private void handleActionCreateShare(MediaShare mediaShare) {

        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;
        long returnValue = dbUtils.insertLocalShare(mediaShare);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(Util.APPLICATION_CONTEXT);
        Intent intent = new Intent(Util.LOCAL_SHARE_CREATED);

        if(returnValue > 0){
            intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.SUCCEED.name());
            intent.putExtra(Util.OPERATION_MEDIASHARE,mediaShare);

            Log.i(TAG,"insert local mediashare succeed");

            MediaShare mapResult = LocalCache.LocalMediaShareMapKeyIsUUID.put(mediaShare.getUuid(),mediaShare);

            Log.i(TAG,"insert local media share to map result:" + (mapResult != null?"true":"false"));

        }else {
            intent.putExtra(Util.OPERATION_RESULT_NAME,OperationResult.FAIL.name());

            Log.i(TAG,"insert local mediashare fail");
        }

        broadcastManager.sendBroadcast(intent);

    }

}
