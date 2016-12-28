package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteMediaParser;
import com.winsun.fruitmix.util.FNAS;
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
public class RetrieveRemoteMediaService extends IntentService {

    public static final String TAG = RetrieveRemoteMediaService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RETRIEVE_REMOTE_MEDIA = "com.winsun.fruitmix.services.action.retrieve_remote_media";

    public RetrieveRemoteMediaService() {
        super("RetrieveRemoteMediaService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRetrieveRemoteMedia(Context context) {
        Intent intent = new Intent(context, RetrieveRemoteMediaService.class);
        intent.setAction(ACTION_RETRIEVE_REMOTE_MEDIA);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RETRIEVE_REMOTE_MEDIA.equals(action)) {
                handleActionRetrieveRemoteMedia();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveRemoteMedia() {

        List<Media> medias;

        ConcurrentMap<String, Media> mediaConcurrentMap;
        DBUtils dbUtils = DBUtils.getInstance(this);

        try {

            HttpResponse httpResponse = FNAS.loadMedia();

            Log.i(TAG, "handleActionRetrieveRemoteMedia: load media finish");

            RemoteDataParser<Media> parser = new RemoteMediaParser();
            medias = parser.parse(httpResponse.getResponseData());

            Log.i(TAG, "handleActionRetrieveRemoteMedia: parse json finish");

            mediaConcurrentMap = LocalCache.BuildMediaMapKeyIsUUID(medias);

            Log.i(TAG, "handleActionRetrieveRemoteMedia: build media map");

            dbUtils.deleteAllRemoteMedia();

            Log.i(TAG, "handleActionRetrieveRemoteMedia: delete all remote media");

            long result = dbUtils.insertRemoteMedias(mediaConcurrentMap);

            Log.i(TAG, "handleActionRetrieveRemoteMedia: insert all remote media result:" + result);

        } catch (Exception e) {
            e.printStackTrace();

            medias = dbUtils.getAllRemoteMedia();

            mediaConcurrentMap = LocalCache.BuildMediaMapKeyIsUUID(medias);

            Log.i(TAG, "handleActionRetrieveRemoteMedia: retrieve media from db");
        }

        LocalCache.RemoteMediaMapKeyIsUUID.clear();

        LocalCache.RemoteMediaMapKeyIsUUID.putAll(mediaConcurrentMap);

        OperationEvent operationEvent = new OperationEvent(Util.REMOTE_MEDIA_RETRIEVED, new OperationSuccess());
        EventBus.getDefault().postSticky(operationEvent);

    }

}
