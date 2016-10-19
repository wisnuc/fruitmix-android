package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

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
    public static void startActionModifyRemoteMediaShare(Context context, MediaShare mediaShare,String requestData) {
        Intent intent = new Intent(context, ModifyRemoteMediaShareService.class);
        intent.setAction(ACTION_MODIFY_REMOTE_MEDIA_SHARE);
        intent.putExtra(EXTRA_MEDIA_SHARE, mediaShare);
        intent.putExtra(EXTRA_REQUEST_DATA,requestData);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MODIFY_REMOTE_MEDIA_SHARE.equals(action)) {
                MediaShare mediaShare = intent.getParcelableExtra(EXTRA_MEDIA_SHARE);
                String requestData = intent.getStringExtra(EXTRA_REQUEST_DATA);
                handleActionModifyRemoteShare(mediaShare,requestData);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionModifyRemoteShare(MediaShare mediaShare,String requestData) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);

        Intent intent = new Intent(Util.REMOTE_SHARE_MODIFIED);

        if (mediaShare.isLocal()) {
            intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.LOCAL_MEDIASHARE_UPLOADING.name());

        } else if (!Util.uploadImageDigestsIfNotUpload(this,mediaShare.getMediaDigestInMediaShareContents())) {

            intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.FAIL.name());

        } else {

            try {
                String result = FNAS.PostRemoteCall(String.format(getString(R.string.update_mediashare_url),Util.MEDIASHARE_PARAMETER,mediaShare.getUuid()), requestData);

                if(result.length() > 0){
                    intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.SUCCEED.name());
                    intent.putExtra(Util.OPERATION_MEDIASHARE,mediaShare);

                    Log.i(TAG,"modify remote share succeed");

                    DBUtils dbUtils = DBUtils.getInstance(this);
                    long dbResult = dbUtils.updateRemoteShare(mediaShare);

                    Log.i(TAG, "modify media in remote mediashare which source is network result:" + dbResult);

                    MediaShare mapResult = LocalCache.RemoteMediaShareMapKeyIsUUID.put(mediaShare.getUuid(), mediaShare);

                    Log.i(TAG, "modify media in remote mediashare in map result:" + (mapResult != null ? "true" : "false"));


                }

            } catch (Exception e) {

                e.printStackTrace();

                intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.FAIL.name());

                Log.i(TAG,"modify remote share fail");
            }
        }

        broadcastManager.sendBroadcast(intent);

    }

}
