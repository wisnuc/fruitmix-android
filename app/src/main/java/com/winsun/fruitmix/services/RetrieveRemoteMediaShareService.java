package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteMediaShareParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResultType;
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

    private static final String LOAD_MEDIASHARE_IN_DB_WHEN_EXCEPTION_OCCUR = "com.winsun.fruitmix.services.action.load_mediashare_in_db_when_exception_occur";

    public RetrieveRemoteMediaShareService() {
        super("RetrieveRemoteMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRetrieveRemoteMediaShare(Context context, boolean loadMediaShareInDBWhenExceptionOccur) {
        Intent intent = new Intent(context, RetrieveRemoteMediaShareService.class);
        intent.setAction(ACTION_GET_REMOTE_MEDIASHARE);
        intent.putExtra(LOAD_MEDIASHARE_IN_DB_WHEN_EXCEPTION_OCCUR, loadMediaShareInDBWhenExceptionOccur);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_REMOTE_MEDIASHARE.equals(action)) {
                handleActionRetrieveRemoteMediaShare(intent.getBooleanExtra(LOAD_MEDIASHARE_IN_DB_WHEN_EXCEPTION_OCCUR, true));
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveRemoteMediaShare(boolean loadMediaShareInDBWhenExceptionOccur) {

        List<MediaShare> mediaShares;

        ConcurrentMap<String, MediaShare> mediaShareConcurrentMap;
        DBUtils dbUtils = DBUtils.getInstance(this);

        try {

            HttpResponse httpResponse = FNAS.loadRemoteShare();

            Log.i(TAG, "loadRemoteShare:" + httpResponse.getResponseData().equals(""));

            RemoteDataParser<MediaShare> parser = new RemoteMediaShareParser();
            mediaShares = parser.parse(httpResponse.getResponseData());

            Log.i(TAG, "handleActionRetrieveRemoteMediaShare: parse remote media share");

            mediaShareConcurrentMap = LocalCache.BuildMediaShareMapKeyIsUUID(mediaShares);

            Log.i(TAG, "handleActionRetrieveRemoteMediaShare: build media share map");

            dbUtils.deleteAllRemoteShare();

            Log.i(TAG, "handleActionRetrieveRemoteMediaShare: delete all remote share in db");

            dbUtils.insertRemoteMediaShares(mediaShareConcurrentMap);

            Log.i(TAG, "handleActionRetrieveRemoteMediaShare: retrieve remote media share from network");

            fillLocalCacheRemoteMediaShareMap(mediaShareConcurrentMap);

            postEvent();

        } catch (Exception e) {
            e.printStackTrace();

            if (loadMediaShareInDBWhenExceptionOccur) {

                mediaShares = dbUtils.getAllRemoteShare();
                mediaShareConcurrentMap = LocalCache.BuildMediaShareMapKeyIsUUID(mediaShares);

                Log.i(TAG, "handleActionRetrieveRemoteMediaShare: retrieve remote media share from db");

                fillLocalCacheRemoteMediaShareMap(mediaShareConcurrentMap);

                postEvent();

            }

        }


    }

    private void postEvent() {
        OperationEvent operationEvent = new OperationEvent(Util.REMOTE_MEDIA_SHARE_RETRIEVED, new OperationSuccess());
        EventBus.getDefault().post(operationEvent);
    }

    private void fillLocalCacheRemoteMediaShareMap(ConcurrentMap<String, MediaShare> mediaShareConcurrentMap) {
        LocalCache.RemoteMediaShareMapKeyIsUUID.clear();

        LocalCache.RemoteMediaShareMapKeyIsUUID.putAll(mediaShareConcurrentMap);
    }

}
