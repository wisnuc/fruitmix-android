package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.parser.RemoteMediaShareJSONObjectParser;
import com.winsun.fruitmix.parser.RemoteMediaShareParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

import org.json.JSONObject;

import java.util.List;

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
            intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.FAIL.name());
            mManager.sendBroadcast(intent);

            return;
        }

        String[] digests = new String[mediaShare.getImageDigests().size()];
        mediaShare.getImageDigests().toArray(digests);

        String data;

        StringBuilder builder = new StringBuilder();
        builder.append("{\"album\":");
        if(mediaShare.isAlbum()){
            builder.append("{\"title\":\"");
            builder.append(mediaShare.getTitle());
            builder.append("\",\"text\":\"");
            builder.append(mediaShare.getDesc());
            builder.append("\"}");
        }else {
            builder.append("null");
        }

        builder.append(",");

        builder.append("\"sticky\":");
        builder.append(mediaShare.isSticky());

        builder.append(",");

        builder.append("\"viewers\":[");
        StringBuilder viewersBuilder = new StringBuilder();
        for (String viewer:mediaShare.getViewers()){
            viewersBuilder.append(",");
            viewersBuilder.append("\"");
            viewersBuilder.append(viewer);
            viewersBuilder.append("\"");
        }
        viewersBuilder.append("]");
        builder.append(viewersBuilder.toString().substring(1));

        builder.append(",");

        builder.append("\"maintainers\":[");
        StringBuilder maintainersBuilder = new StringBuilder();
        for (String maintainer :mediaShare.getMaintainers()){
            maintainersBuilder.append(",");
            maintainersBuilder.append("\"");
            maintainersBuilder.append(maintainer);
            maintainersBuilder.append("\"");
        }
        maintainersBuilder.append("]");
        builder.append(maintainersBuilder.toString().substring(1));

        builder.append(",");

        builder.append("\"contents\":[");
        StringBuilder contentsBuilder = new StringBuilder();
        for (String content :mediaShare.getImageDigests()){
            contentsBuilder.append(",");
            contentsBuilder.append("\"");
            contentsBuilder.append(content);
            contentsBuilder.append("\"");
        }
        contentsBuilder.append("]");
        builder.append(contentsBuilder.toString().substring(1));

        builder.append("}");
        data = builder.toString();
        Log.i(TAG, "handleActionCreateRemoteMediaShareTask: request json:" + data);

        String result = "";

        try {
            result = FNAS.PostRemoteCall(Util.MEDIASHARE_PARAMETER, data);

            if (result.length() > 0) {

                Log.i(TAG, "insert remote mediashare which source is network succeed");

                intent.putExtra(Util.OPERATION_MEDIASHARE,mediaShare.cloneMyself());

                RemoteMediaShareJSONObjectParser parser = new RemoteMediaShareJSONObjectParser();

                String  mediaShareUUID = parser.getRemoteMediaShare(new JSONObject(result)).getUuid();

                mediaShare.setUuid(mediaShareUUID);
                mediaShare.setLocal(false);

                DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;
                long dbResult = dbUtils.insertRemoteMediaShare(mediaShare);

                Log.i(TAG, "insert remote mediashare which source is db result:" + dbResult);

                MediaShare mapResult = LocalCache.RemoteMediaShareMapKeyIsUUID.put(mediaShare.getUuid(), mediaShare);

                Log.i(TAG, "insert remote mediashare to map result:" + (mapResult != null ? "true" : "false"));

                intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.SUCCEED.name());

            }

        } catch (Exception ex) {
            ex.printStackTrace();

            if(result.length() == 0){
                intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.FAIL.name());
                Log.i(TAG, "insert remote mediashare fail");
            }

        }

        mManager.sendBroadcast(intent);

    }

}
