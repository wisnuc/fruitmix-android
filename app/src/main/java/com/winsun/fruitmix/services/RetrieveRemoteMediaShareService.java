package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteMediaShareParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

    /*
     * has memory issue,cause too much gc
     */
    @Deprecated
    private void handleActionRetrieveRemoteMediaShares(boolean loadMediaShareInDBWhenExceptionOccur) {

        List<MediaShare> remoteMediaSharesFromNetwork;
        List<MediaShare> remoteMediaSharesFromDB;

        List<MediaShare> newMediaShares = new ArrayList<>();
        List<MediaShare> oldMediaShares = new ArrayList<>();

        ConcurrentMap<String, MediaShare> mediaShareConcurrentMap;

        Collection<String> oldMediaShareMapKey;
        Collection<String> newMediaShareMapKey;

        DBUtils dbUtils = DBUtils.getInstance(this);

        if (LocalCache.RemoteMediaShareMapKeyIsUUID.isEmpty()) {
            remoteMediaSharesFromDB = dbUtils.getAllRemoteShare();
            mediaShareConcurrentMap = LocalCache.BuildMediaShareMapKeyIsUUID(remoteMediaSharesFromDB);

            Log.i(TAG, "handleActionRetrieveRemoteMediaShare: retrieve remote media share from db");

            fillLocalCacheRemoteMediaShareMap(mediaShareConcurrentMap);
        }

        try {
            HttpResponse httpResponse = FNAS.loadRemoteShare();

            Log.i(TAG, "loadRemoteShare:" + httpResponse.getResponseData().equals(""));

            RemoteDataParser<MediaShare> parser = new RemoteMediaShareParser();
            remoteMediaSharesFromNetwork = parser.parse(httpResponse.getResponseData());

            mediaShareConcurrentMap = LocalCache.BuildMediaShareMapKeyIsUUID(remoteMediaSharesFromNetwork);

            oldMediaShareMapKey = LocalCache.RemoteMediaShareMapKeyIsUUID.keySet();
            newMediaShareMapKey = mediaShareConcurrentMap.keySet();

            Log.i(TAG, "handleActionRetrieveRemoteMediaShare: parse remote media share" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

            calcOldMediaSharesAndNewMediaShares(newMediaShares, oldMediaShares, mediaShareConcurrentMap, oldMediaShareMapKey, newMediaShareMapKey);

            calcNewMediaShares(newMediaShares, mediaShareConcurrentMap, oldMediaShareMapKey, newMediaShareMapKey);

            handleOldAndNewMediaShares(newMediaShares, oldMediaShares, dbUtils);

            postEvent();

        } catch (Exception ex) {
            ex.printStackTrace();

            if (loadMediaShareInDBWhenExceptionOccur) {
                postEvent();
            }
        }

    }

    private void handleOldAndNewMediaShares(List<MediaShare> newMediaShares, List<MediaShare> oldMediaShares, DBUtils dbUtils) {
        if (!oldMediaShares.isEmpty() || !newMediaShares.isEmpty()) {
            dbUtils.deleteOldAndInsertNewRemoteMediaShare(oldMediaShares, newMediaShares);

            Log.i(TAG, "handleActionRetrieveRemoteMediaShare: finish delete old and insert new mediashares " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

        } else {
            Log.i(TAG, "handleActionRetrieveRemoteMediaShare: no oldMediaShares or newMediaShares");
        }
    }

    private void calcNewMediaShares(List<MediaShare> newMediaShares, ConcurrentMap<String, MediaShare> mediaShareConcurrentMap, Collection<String> oldMediaShareMapKey, Collection<String> newMediaShareMapKey) {
        for (String newKey : newMediaShareMapKey) {

            if (!oldMediaShareMapKey.contains(newKey)) {

                MediaShare mediaShare = mediaShareConcurrentMap.get(newKey);

                newMediaShares.add(mediaShare);

                LocalCache.RemoteMediaShareMapKeyIsUUID.putIfAbsent(newKey, mediaShare);
            }

        }

        Log.d(TAG, "handleActionRetrieveRemoteMediaShare: finish calc newMediaShares " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
    }

    private void calcOldMediaSharesAndNewMediaShares(List<MediaShare> newMediaShares, List<MediaShare> oldMediaShares, ConcurrentMap<String, MediaShare> mediaShareConcurrentMap, Collection<String> oldMediaShareMapKey, Collection<String> newMediaShareMapKey) {
        for (String oldKey : oldMediaShareMapKey) {

            if (newMediaShareMapKey.contains(oldKey)) {

                MediaShare oldMediaShare = LocalCache.RemoteMediaShareMapKeyIsUUID.get(oldKey);
                MediaShare newMediaShare = mediaShareConcurrentMap.get(oldKey);

                if (!newMediaShare.getShareDigest().equals(oldMediaShare.getShareDigest())) {

                    oldMediaShares.add(oldMediaShare);
                    newMediaShares.add(newMediaShare);

                    LocalCache.RemoteMediaShareMapKeyIsUUID.put(oldKey, newMediaShare);
                }

            } else {
                oldMediaShares.add(LocalCache.RemoteMediaShareMapKeyIsUUID.get(oldKey));

                LocalCache.RemoteMediaShareMapKeyIsUUID.remove(oldKey);
            }

        }

        Log.d(TAG, "handleActionRetrieveRemoteMediaShare: finish calc oldMediaShares and newMediaShares " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
    }

    private void handleActionRetrieveRemoteMediaShare(boolean loadMediaShareInDBWhenExceptionOccur) {

        List<MediaShare> mediaShares;

        ConcurrentMap<String, MediaShare> mediaShareConcurrentMap;
        DBUtils dbUtils = DBUtils.getInstance(this);

        List<String> oldMediaSharesDigests;
        List<String> newMediaSharesDigests;

        try {

            HttpResponse httpResponse = FNAS.loadRemoteShare();

            Log.d(TAG, "loadRemoteShare:" + httpResponse.getResponseData().equals(""));

            RemoteDataParser<MediaShare> parser = new RemoteMediaShareParser();
            mediaShares = parser.parse(httpResponse.getResponseData());

            Log.d(TAG, "handleActionRetrieveRemoteMediaShare: parse remote media share");

            newMediaSharesDigests = new ArrayList<>(mediaShares.size());
            for (MediaShare mediaShare : mediaShares) {
                newMediaSharesDigests.add(mediaShare.getShareDigest());
            }

            oldMediaSharesDigests = new ArrayList<>(LocalCache.RemoteMediaShareMapKeyIsUUID.size());
            for (MediaShare mediaShare : LocalCache.RemoteMediaShareMapKeyIsUUID.values()) {
                oldMediaSharesDigests.add(mediaShare.getShareDigest());
            }

            Log.d(TAG, "handleActionRetrieveRemoteMediaShare: generate oldMediaShares and newMediaShares");

            if (oldMediaSharesDigests.containsAll(newMediaSharesDigests) && newMediaSharesDigests.containsAll(oldMediaSharesDigests)) {

                Log.d(TAG, "handleActionRetrieveRemoteMediaShare: old media shares are same as newMediaShares");

                if (!loadMediaShareInDBWhenExceptionOccur) {

                    Util.refreshMediaShareDelayTime = Util.refreshMediaShareDelayTime * 2;

                    if (Util.refreshMediaShareDelayTime > Util.MAX_REFRESH_MEDIA_SHARE_DELAY_TIME) {
                        Util.refreshMediaShareDelayTime = Util.MAX_REFRESH_MEDIA_SHARE_DELAY_TIME;
                    }

                    return;
                }

            } else {
                Util.refreshMediaShareDelayTime = Util.DEFAULT_REFRESH_MEDIA_SHARE_DELAY_TIME;
            }

            dbUtils.deleteAllRemoteShare();

            Log.d(TAG, "handleActionRetrieveRemoteMediaShare: delete all remote share in db");

            mediaShareConcurrentMap = LocalCache.BuildMediaShareMapKeyIsUUID(mediaShares);

            Log.d(TAG, "handleActionRetrieveRemoteMediaShare: build media share map");

            dbUtils.insertRemoteMediaShares(mediaShareConcurrentMap.values());

            Log.i(TAG, "handleActionRetrieveRemoteMediaShare: retrieve remote media share from network");

            fillLocalCacheRemoteMediaShareMap(mediaShareConcurrentMap);

            Util.setRemoteMediaShareLoaded(true);

            postEvent();

        } catch (Exception e) {
            e.printStackTrace();

            if (loadMediaShareInDBWhenExceptionOccur) {

                mediaShares = dbUtils.getAllRemoteShare();

                mediaShareConcurrentMap = LocalCache.BuildMediaShareMapKeyIsUUID(mediaShares);

                Log.i(TAG, "handleActionRetrieveRemoteMediaShare: retrieve remote media share from db");

                fillLocalCacheRemoteMediaShareMap(mediaShareConcurrentMap);

                Util.setRemoteMediaShareLoaded(true);

                postEvent();

            }

        }

        ButlerService.startTimingRetrieveMediaShare();

    }

    private void postEvent() {
        OperationEvent operationEvent = new OperationEvent(Util.REMOTE_MEDIA_SHARE_RETRIEVED, new OperationSuccess());
        EventBus.getDefault().post(operationEvent);
    }

    private void fillLocalCacheRemoteMediaShareMap(ConcurrentMap<String, MediaShare> mediaShareConcurrentMap) {

        if (LocalCache.RemoteMediaShareMapKeyIsUUID == null) return;

        LocalCache.RemoteMediaShareMapKeyIsUUID.clear();

        LocalCache.RemoteMediaShareMapKeyIsUUID.putAll(mediaShareConcurrentMap);
    }

}
