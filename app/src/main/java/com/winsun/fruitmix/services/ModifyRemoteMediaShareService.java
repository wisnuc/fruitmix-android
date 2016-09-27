package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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

    // TODO: Rename parameters
    private static final String EXTRA_MEDIA_SHARE = "com.winsun.fruitmix.services.extra.share";

    public ModifyRemoteMediaShareService() {
        super("ModifyRemoteMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionModifyRemoteMediaShare(Context context, MediaShare mediaShare) {
        Intent intent = new Intent(context, ModifyRemoteMediaShareService.class);
        intent.setAction(ACTION_MODIFY_REMOTE_MEDIA_SHARE);
        intent.putExtra(EXTRA_MEDIA_SHARE, mediaShare);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MODIFY_REMOTE_MEDIA_SHARE.equals(action)) {
                final MediaShare mediaShare = intent.getParcelableExtra(EXTRA_MEDIA_SHARE);
                handleActionModifyRemoteShare(mediaShare);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionModifyRemoteShare(MediaShare mediaShare) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);

        Intent intent = new Intent(Util.REMOTE_SHARE_MODIFIED);

        String viewers;
        String maintainers;

        if (mediaShare.isLocked()) {
            intent.putExtra(Util.OPERATION_RESULT, OperationResult.LOCAL_MEDIASHARE_UPLOADING.name());

        } else if (!Util.uploadImageDigestsIfNotUpload(this,mediaShare.getImageDigests())) {

            intent.putExtra(Util.OPERATION_RESULT, OperationResult.FAIL.name());

        } else {

            viewers = "";
            for (String key : mediaShare.getViewer()) {
                viewers += ",\\\"" + key + "\\\"";
            }
            if (viewers.length() == 0) {
                viewers += ",";
            }
            Log.i(TAG, "winsun viewer:" + viewers);

            maintainers = "";
            for (String key : mediaShare.getMaintainer()) {
                maintainers += ",\\\"" + key + "\\\"";
            }

            Log.i(TAG, "winsun maintainers:" + maintainers);

            viewers = "{\"commands\": \"[{\\\"op\\\":\\\"replace\\\", \\\"path\\\":\\\"" + mediaShare.getUuid() + "\\\", \\\"value\\\":{\\\"archived\\\":\\\"false\\\",\\\"album\\\":\\\"true\\\", \\\"maintainers\\\":[\\\"" + maintainers.substring(1) + "\\\"], \\\"tags\\\":[{\\\"albumname\\\":\\\"" + mediaShare.getTitle() + "\\\", \\\"desc\\\":\\\"" + mediaShare.getDesc() + "\\\"}], \\\"viewers\\\":[" + viewers.substring(1) + "]}}]\"}";
            try {
                String result = FNAS.PatchRemoteCall(Util.MEDIASHARE_PARAMETER, viewers);

                if(result.length() > 0){
                    intent.putExtra(Util.OPERATION_RESULT, OperationResult.SUCCEED.name());
                    intent.putExtra(Util.OPERATION_MEDIASHARE,mediaShare);

                    Log.i(TAG,"modify remote share succeed");

                    DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;
                    long dbResult = dbUtils.updateRemoteShare(mediaShare);

                    Log.i(TAG, "modify media in remote mediashare which source is network result:" + dbResult);

                    MediaShare mapResult = LocalCache.RemoteMediaShareMapKeyIsUUID.put(mediaShare.getUuid(), mediaShare);

                    Log.i(TAG, "modify media in remote mediashare in map result:" + (mapResult != null ? "true" : "false"));


                }

            } catch (Exception e) {

                e.printStackTrace();

                intent.putExtra(Util.OPERATION_RESULT, OperationResult.FAIL.name());

                Log.i(TAG,"modify remote share fail");
            }
        }

        broadcastManager.sendBroadcast(intent);

    }

}
