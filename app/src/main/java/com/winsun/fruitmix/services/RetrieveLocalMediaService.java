package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResultType;
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
public class RetrieveLocalMediaService extends IntentService {

    private static final String TAG = RetrieveLocalMediaService.class.getSimpleName();

    private static final String ACTION_RETRIEVE_LOCAL_MEDIA = "com.winsun.fruitmix.services.action.retrieve_local_media";

    public RetrieveLocalMediaService() {
        super("RetrieveLocalMediaService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRetrieveLocalMedia(Context context) {
        Intent intent = new Intent(context, RetrieveLocalMediaService.class);
        intent.setAction(ACTION_RETRIEVE_LOCAL_MEDIA);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RETRIEVE_LOCAL_MEDIA.equals(action)) {
                handleActionRetrieveLocalMedia();
            }
        }
    }

    /**
     * Handle action retrieve local media in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveLocalMedia() {

        List<Media> medias;

        ConcurrentMap<String, Media> mediaConcurrentMap;
        DBUtils dbUtils = DBUtils.getInstance(this);

        medias = dbUtils.getAllLocalMedia();

        mediaConcurrentMap = LocalCache.BuildMediaMapKeyIsThumb(medias);

        LocalCache.LocalMediaMapKeyIsThumb.clear();

        LocalCache.LocalMediaMapKeyIsThumb.putAll(mediaConcurrentMap);

        LocalCache.BuildLocalImagesMapsKeyIsUUID();

        OperationEvent operationEvent = new OperationEvent(Util.LOCAL_MEDIA_RETRIEVED, OperationResultType.SUCCEED);
        EventBus.getDefault().post(operationEvent);

    }
}
