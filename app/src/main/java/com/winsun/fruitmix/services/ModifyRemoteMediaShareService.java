package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.MediaShareOperationEvent;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationLocalMediaShareUploading;
import com.winsun.fruitmix.model.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ModifyRemoteMediaShareService extends IntentService {

    private static final String TAG = ModifyRemoteMediaShareService.class.getSimpleName();

    private static final String ACTION_MODIFY_REMOTE_MEDIA_SHARE = "com.winsun.fruitmix.services.action.modify.remote.share";

    private static final String EXTRA_MEDIA_SHARE = "com.winsun.fruitmix.services.extra.share";

    private static final String EXTRA_REQUEST_DATA = "com.wisnun.fruitmix.services.extra.request.data";

    public ModifyRemoteMediaShareService() {
        super("ModifyRemoteMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionModifyRemoteMediaShare(Context context, MediaShare mediaShare, String requestData) {
        Intent intent = new Intent(context, ModifyRemoteMediaShareService.class);
        intent.setAction(ACTION_MODIFY_REMOTE_MEDIA_SHARE);

        mediaShare.clearMediaShareContents();

        intent.putExtra(EXTRA_MEDIA_SHARE, mediaShare);
        intent.putExtra(EXTRA_REQUEST_DATA, requestData);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MODIFY_REMOTE_MEDIA_SHARE.equals(action)) {
                MediaShare mediaShare = intent.getParcelableExtra(EXTRA_MEDIA_SHARE);
                String requestData = intent.getStringExtra(EXTRA_REQUEST_DATA);
                handleActionModifyRemoteShare(mediaShare, requestData);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionModifyRemoteShare(MediaShare mediaShare, String requestData) {

        MediaShareOperationEvent mediaShareOperationEvent;

        if (mediaShare.isLocal()) {

            mediaShareOperationEvent = new MediaShareOperationEvent(Util.REMOTE_SHARE_MODIFIED, new OperationLocalMediaShareUploading(), mediaShare);

        } else {

            try {
                HttpResponse httpResponse = FNAS.PostRemoteCall(String.format(getString(R.string.android_update_mediashare_url), Util.MEDIASHARE_PARAMETER, mediaShare.getUuid()), requestData);

                if (httpResponse.getResponseCode() == 200) {

                    mediaShareOperationEvent = new MediaShareOperationEvent(Util.REMOTE_SHARE_MODIFIED, new OperationSuccess(), mediaShare);

                    Log.i(TAG, "modify remote share succeed");

                    DBUtils dbUtils = DBUtils.getInstance(this);
                    long dbResult = dbUtils.updateRemoteMediaShare(mediaShare);

                    Log.i(TAG, "modify media in remote mediashare which source is network result:" + dbResult);

                    updateMediaShareInLocalCacheMap(mediaShare);

                } else {
                    mediaShareOperationEvent = new MediaShareOperationEvent(Util.REMOTE_SHARE_MODIFIED, new OperationNetworkException(httpResponse.getResponseCode()), mediaShare);
                }

            } catch (MalformedURLException e) {

                e.printStackTrace();

                mediaShareOperationEvent = new MediaShareOperationEvent(Util.REMOTE_SHARE_MODIFIED, new OperationMalformedUrlException(), mediaShare);

                Log.i(TAG, "modify remote share fail");
            } catch (SocketTimeoutException e) {

                e.printStackTrace();

                mediaShareOperationEvent = new MediaShareOperationEvent(Util.REMOTE_SHARE_MODIFIED, new OperationSocketTimeoutException(), mediaShare);

                Log.i(TAG, "modify remote share fail");
            } catch (IOException e) {

                e.printStackTrace();

                mediaShareOperationEvent = new MediaShareOperationEvent(Util.REMOTE_SHARE_MODIFIED, new OperationIOException(), mediaShare);

                Log.i(TAG, "modify remote share fail");
            }
        }

        EventBus.getDefault().post(mediaShareOperationEvent);
    }

    private void updateMediaShareInLocalCacheMap(MediaShare mediaShare) {
        MediaShare originalMediaShare = LocalCache.RemoteMediaShareMapKeyIsUUID.get(mediaShare.getUuid());

        originalMediaShare.setTitle(mediaShare.getTitle());
        originalMediaShare.setDesc(mediaShare.getDesc());
        originalMediaShare.clearViewers();
        originalMediaShare.addViewers(mediaShare.getViewers());
        originalMediaShare.clearMaintainers();
        originalMediaShare.addMaintainers(mediaShare.getMaintainers());
    }

}
