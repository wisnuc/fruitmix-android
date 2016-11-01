package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteMediaShareParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class RetrieveRemoteMediaShareService extends IntentService {

    private static final String TAG = RetrieveRemoteMediaShareService.class.getSimpleName();

    private static final String ACTION_GET_REMOTE_MEDIASHARE = "com.winsun.fruitmix.services.action.retrieve.remote.mediashare";

    public RetrieveRemoteMediaShareService() {
        super("RetrieveRemoteDataService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRetrieveRemoteMediaShare(Context context) {
        Intent intent = new Intent(context, RetrieveRemoteMediaShareService.class);
        intent.setAction(ACTION_GET_REMOTE_MEDIASHARE);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_REMOTE_MEDIASHARE.equals(action)) {
                handleActionRetrieveRemoteMediaShare();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveRemoteMediaShare() {

        List<MediaShare> mediaShares;

        ConcurrentMap<String, MediaShare> mediaShareConcurrentMap;
        DBUtils dbUtils = DBUtils.getInstance(this);

        try {

            String json = FNAS.loadRemoteShare();

            Log.i(TAG, "loadRemoteShare:"+json.equals(""));

            RemoteDataParser<MediaShare> parser = new RemoteMediaShareParser();
            mediaShares = parser.parse(json);
            mediaShareConcurrentMap = LocalCache.BuildMediaShareMapKeyIsUUID(mediaShares);

            dbUtils.deleteAllRemoteShare();
            dbUtils.insertRemoteMediaShares(mediaShareConcurrentMap);

        } catch (Exception e) {
            e.printStackTrace();

            mediaShares = dbUtils.getAllRemoteShare();
            mediaShareConcurrentMap = LocalCache.BuildMediaShareMapKeyIsUUID(mediaShares);

        }

        LocalCache.RemoteMediaShareMapKeyIsUUID.clear();

        LocalCache.RemoteMediaShareMapKeyIsUUID.putAll(mediaShareConcurrentMap);

        Log.i(TAG, "retrieve remote media share from network");

        OperationEvent operationEvent = new OperationEvent(Util.REMOTE_MEDIA_SHARE_RETRIEVED,OperationResult.SUCCEED);
        EventBus.getDefault().post(operationEvent);

    }

}
