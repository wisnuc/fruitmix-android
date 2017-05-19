package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.NewPhotoListDataLoader;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteMediaParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
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

            Log.i(TAG, "handleActionRetrieveRemoteMedia: before load" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

            HttpResponse httpResponse = FNAS.loadMedia(this);

            Log.i(TAG, "handleActionRetrieveRemoteMedia: load media finish" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

            RemoteDataParser<Media> parser = new RemoteMediaParser();
            medias = parser.parse(httpResponse.getResponseData());

//            medias = Collections.emptyList();

            Log.i(TAG, "handleActionRetrieveRemoteMedia: parse json finish");

            mediaConcurrentMap = LocalCache.BuildMediaMapKeyIsUUID(medias);

            Log.i(TAG, "handleActionRetrieveRemoteMedia: build media map");

            fillRemoteMediaMap(mediaConcurrentMap);

            Util.setRemoteMediaLoaded(true);
            NewPhotoListDataLoader.INSTANCE.setNeedRefreshData(true);

            sendEvent();

            Log.i(TAG, "handleActionRetrieveRemoteMedia: before delete all remote media" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

            dbUtils.deleteAllRemoteMedia();

            Log.i(TAG, "handleActionRetrieveRemoteMedia: after delete all remote media" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

            long result = dbUtils.insertRemoteMedias(mediaConcurrentMap);

            Log.i(TAG, "handleActionRetrieveRemoteMedia: insert all remote media result:" + result + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        } catch (Exception e) {
            e.printStackTrace();

            medias = dbUtils.getAllRemoteMedia();

            mediaConcurrentMap = LocalCache.BuildMediaMapKeyIsUUID(medias);

            Log.i(TAG, "handleActionRetrieveRemoteMedia: retrieve media from db");

            fillRemoteMediaMap(mediaConcurrentMap);

            Util.setRemoteMediaLoaded(true);
            NewPhotoListDataLoader.INSTANCE.setNeedRefreshData(true);

            sendEvent();

        }

        FNAS.retrieveRemoteMediaShare(this, true);

    }

    private void fillRemoteMediaMap(ConcurrentMap<String, Media> mediaConcurrentMap) {
        LocalCache.RemoteMediaMapKeyIsUUID.clear();
        LocalCache.RemoteMediaMapKeyIsUUID.putAll(mediaConcurrentMap);
    }

    private void sendEvent() {
        OperationEvent operationEvent = new OperationEvent(Util.REMOTE_MEDIA_RETRIEVED, new OperationSuccess(R.string.operate));
        EventBus.getDefault().post(operationEvent);
    }

}
