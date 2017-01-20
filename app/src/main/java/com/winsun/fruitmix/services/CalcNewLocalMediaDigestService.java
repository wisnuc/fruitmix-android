package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationNoChanged;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CalcNewLocalMediaDigestService extends IntentService {

    private static final String TAG = CalcNewLocalMediaDigestService.class.getSimpleName();

    public CalcNewLocalMediaDigestService() {
        super("CalcNewLocalMediaDigestService");
    }

    private static final String ACTION_CALC_NEW_LOCAL_MEDIA_DIGEST = "com.winsun.fruitmix.services.action.calc.new.local.media.digest";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionCalcNewLocalMediaDigest(Context context) {
        Intent intent = new Intent(context, CalcNewLocalMediaDigestService.class);
        intent.setAction(ACTION_CALC_NEW_LOCAL_MEDIA_DIGEST);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CALC_NEW_LOCAL_MEDIA_DIGEST.equals(action)) {
                handleActionCalcNewLocalMediaDigest();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCalcNewLocalMediaDigest() {

        Collection<Media> medias = LocalCache.LocalMediaMapKeyIsThumb.values();

        List<Media> newMediaList = new ArrayList<>();

        OperationEvent operationEvent;

        Log.d(TAG, "handleActionCalcNewLocalMediaDigest: start calc media digest" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        for (Media media : medias) {
            if (media.getUuid().isEmpty()) {
                String uuid = Util.CalcSHA256OfFile(media.getThumb());
                media.setUuid(uuid);

                newMediaList.add(media);
            }
        }

        Log.d(TAG, "handleActionCalcNewLocalMediaDigest: end calc media digest" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        if (newMediaList.size() > 0) {

            DBUtils dbUtils = DBUtils.getInstance(this);

            long returnValue = dbUtils.insertLocalMedias(newMediaList);

            Log.i(TAG, "insert local media result:" + returnValue + " time:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

            operationEvent = new OperationEvent(Util.CALC_NEW_LOCAL_MEDIA_DIGEST_FINISHED, new OperationSuccess());
        } else {
            operationEvent = new OperationEvent(Util.CALC_NEW_LOCAL_MEDIA_DIGEST_FINISHED, new OperationNoChanged());
        }

        EventBus.getDefault().post(operationEvent);
    }

}
