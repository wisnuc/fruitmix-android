package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteMediaParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

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
    // TODO: Rename actions, choose action names that describe tasks that this
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

            String json = FNAS.loadMedia();

            RemoteDataParser<Media> parser = new RemoteMediaParser();
            medias = parser.parse(json);

            mediaConcurrentMap = LocalCache.BuildMediaMapKeyIsUUID(medias);

            dbUtils.deleteAllRemoteMedia();
            dbUtils.insertRemoteMedias(mediaConcurrentMap);

        } catch (Exception e) {
            e.printStackTrace();

            medias = dbUtils.getAllRemoteMedia();

            mediaConcurrentMap = LocalCache.BuildMediaMapKeyIsUUID(medias);
        }

        LocalCache.RemoteMediaMapKeyIsUUID.clear();

        LocalCache.RemoteMediaMapKeyIsUUID.putAll(mediaConcurrentMap);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(Util.REMOTE_MEDIA_RETRIEVED);
        intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.SUCCEED.name());
        localBroadcastManager.sendBroadcast(intent);

    }

}
