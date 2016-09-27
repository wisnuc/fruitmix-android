package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteMediaShareJSONObjectParser;
import com.winsun.fruitmix.parser.RemoteMediaShareParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class CreateRemoteMediaShareService extends IntentService {

    public static final String TAG = CreateRemoteMediaShareService.class.getSimpleName();

    // TODO: Rename actions, choose action names that describe tasks that this
    private static final String ACTION_CREATE_REMOTE_MEDIASHARE_TASK = "com.winsun.fruitmix.action.create.remote.mediashare";

    private static final String EXTRA_MEDIASHARE = "extra_mediashare";

    private LocalBroadcastManager mManager;

    public CreateRemoteMediaShareService() {
        super("CreateRemoteMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionCreateRemoteMediaShareTask(Context context, MediaShare mediaShare) {
        Intent intent = new Intent(context, CreateRemoteMediaShareService.class);
        intent.setAction(ACTION_CREATE_REMOTE_MEDIASHARE_TASK);
        intent.putExtra(EXTRA_MEDIASHARE, mediaShare);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_REMOTE_MEDIASHARE_TASK.equals(action)) {

                MediaShare mediaShare = intent.getParcelableExtra(EXTRA_MEDIASHARE);

                handleActionCreateRemoteMediaShareTask(mediaShare);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCreateRemoteMediaShareTask(MediaShare mediaShare) {
        // TODO: Handle action create remote mediashare

        mManager = LocalBroadcastManager.getInstance(this.getApplicationContext());

        Intent intent = new Intent(Util.REMOTE_SHARE_CREATED);

        boolean returnValue = Util.uploadImageDigestsIfNotUpload(this, mediaShare.getImageDigests());

        if (!returnValue) {
            intent.putExtra(Util.OPERATION_RESULT, OperationResult.FAIL.name());
            mManager.sendBroadcast(intent);

            return;
        }

        String[] digests = new String[mediaShare.getImageDigests().size()];
        mediaShare.getImageDigests().toArray(digests);

        String data, viewers, maintainers;
        int i;

        data = "";
        for (i = 0; i < digests.length; i++) {
            data += ",{\\\"type\\\":\\\"media\\\",\\\"digest\\\":\\\"" + digests[i] + "\\\"}";
        }

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

        StringBuilder builder = new StringBuilder();
        builder.append("{\"album\":");
        builder.append(String.valueOf(mediaShare.isAlbum()));
        builder.append(", \"archived\":false,\"maintainers\":\"[");
        builder.append(maintainers.substring(1));
        builder.append("]\",\"viewers\":\"[");
        builder.append(viewers.substring(1));
        builder.append("]\",\"tags\":[{");
        if (mediaShare.isAlbum()) {
            builder.append("\"albumname\":\"");
            builder.append(mediaShare.getTitle());
            builder.append("\",\"desc\":\"");
            builder.append(mediaShare.getDesc());
            builder.append("\"");
        }
        builder.append("}],\"contents\":\"[");
        builder.append(data.substring(1));
        builder.append("]\"}");

        data = builder.toString();

        String result = "";

        try {
            result = FNAS.PostRemoteCall(Util.MEDIASHARE_PARAMETER, data);

            if (result.length() > 0) {

                Log.i(TAG, "insert remote mediashare which source is network succeed");

                intent.putExtra(Util.OPERATION_LOCAL_MEDIASHARE_UUID, mediaShare.getUuid());
                intent.putExtra(Util.OPERATION_LOCAL_MEDIASHARE_LOCKED,mediaShare.isLocked());

                RemoteMediaShareJSONObjectParser parser = new RemoteMediaShareJSONObjectParser();

                String remoteMediaShareUUID = parser.getRemoteMediaShareUUID(new JSONObject(result));

                mediaShare.setUuid(remoteMediaShareUUID);
                mediaShare.setLocked(false);

                DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;
                long dbResult = dbUtils.insertRemoteMediaShare(mediaShare);

                Log.i(TAG, "insert remote mediashare which source is db result:" + dbResult);

                MediaShare mapResult = LocalCache.RemoteMediaShareMapKeyIsUUID.put(mediaShare.getUuid(), mediaShare);

                Log.i(TAG, "insert remote mediashare to map result:" + (mapResult != null ? "true" : "false"));

                intent.putExtra(Util.OPERATION_RESULT, OperationResult.SUCCEED.name());

            }

        } catch (Exception ex) {
            ex.printStackTrace();

            if(result.length() == 0){
                intent.putExtra(Util.OPERATION_RESULT, OperationResult.FAIL.name());
                Log.i(TAG, "insert remote mediashare fail");

            }

        }

        mManager.sendBroadcast(intent);

    }

}
