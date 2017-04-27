package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RetrieveLocalMediaShareService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RETRIEVE_LOCAL_MEDIA_SHARE = "com.winsun.fruitmix.services.action.retrieve_local_media_share";

    public RetrieveLocalMediaShareService() {
        super("RetrieveLocalMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRetrieveMediaShare(Context context) {
        Intent intent = new Intent(context, RetrieveLocalMediaShareService.class);
        intent.setAction(ACTION_RETRIEVE_LOCAL_MEDIA_SHARE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RETRIEVE_LOCAL_MEDIA_SHARE.equals(action)) {

                handleActionRetrieveLocalMediaShare();
            }
        }
    }

    /**
     * Handle action retrieve local mediashare in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveLocalMediaShare() {

        List<MediaShare> mediaShares;

        ConcurrentMap<String, MediaShare> mediaShareConcurrentMap;
        DBUtils dbUtils = DBUtils.getInstance(this);

        mediaShares = dbUtils.getAllLocalShare();

        mediaShareConcurrentMap = LocalCache.BuildMediaShareMapKeyIsUUID(mediaShares);

        LocalCache.LocalMediaShareMapKeyIsUUID.clear();

        LocalCache.LocalMediaShareMapKeyIsUUID.putAll(mediaShareConcurrentMap);

        OperationEvent operationEvent = new OperationEvent(Util.LOCAL_MEDIA_SHARE_RETRIEVED, new OperationSuccess(R.string.operate));
        EventBus.getDefault().post(operationEvent);

    }

}
