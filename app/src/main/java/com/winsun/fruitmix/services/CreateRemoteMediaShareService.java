package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.MediaShareOperationEvent;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.operationResult.OperationIOException;
import com.winsun.fruitmix.operationResult.OperationJSONException;
import com.winsun.fruitmix.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.operationResult.OperationNetworkException;
import com.winsun.fruitmix.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.operationResult.OperationSuccess;
import com.winsun.fruitmix.operationResult.OperationUploadPhotoFailed;
import com.winsun.fruitmix.parser.RemoteMediaShareJSONObjectParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

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

        MediaShareOperationEvent mediaShareOperationEvent;

        String data;

        StringBuilder builder = new StringBuilder();
        builder.append("{\"album\":");
        if (mediaShare.isAlbum()) {
            builder.append("{\"title\":\"");
            builder.append(Util.removeWrap(mediaShare.getTitle()));
            builder.append("\",\"text\":\"");
            builder.append(Util.removeWrap(mediaShare.getDesc()));
            builder.append("\"}");
        } else {
            builder.append("null");
        }

        builder.append(",");

        builder.append("\"sticky\":");
        builder.append(mediaShare.isSticky());

        builder.append(",");

        builder.append("\"viewers\":[");
        StringBuilder viewersBuilder = new StringBuilder();
        for (String viewer : mediaShare.getViewers()) {
            viewersBuilder.append(",");
            viewersBuilder.append("\"");
            viewersBuilder.append(viewer);
            viewersBuilder.append("\"");
        }
        viewersBuilder.append("]");
        if (viewersBuilder.length() > 1) {
            builder.append(viewersBuilder.toString().substring(1));
        } else {
            builder.append(viewersBuilder.toString());
        }

        builder.append(",");

        builder.append("\"maintainers\":[");
        StringBuilder maintainersBuilder = new StringBuilder();
        for (String maintainer : mediaShare.getMaintainers()) {
            maintainersBuilder.append(",");
            maintainersBuilder.append("\"");
            maintainersBuilder.append(maintainer);
            maintainersBuilder.append("\"");
        }
        maintainersBuilder.append("]");
        if (maintainersBuilder.length() > 1) {
            builder.append(maintainersBuilder.toString().substring(1));
        } else {
            builder.append(maintainersBuilder.toString());
        }

        builder.append(",");

        builder.append("\"contents\":[");
        StringBuilder contentsBuilder = new StringBuilder();
        for (String content : mediaShare.getMediaDigestInMediaShareContents()) {
            contentsBuilder.append(",");
            contentsBuilder.append("\"");
            contentsBuilder.append(content);
            contentsBuilder.append("\"");
        }
        contentsBuilder.append("]");
        if (contentsBuilder.length() > 1) {
            builder.append(contentsBuilder.toString().substring(1));
        } else {
            builder.append(contentsBuilder.toString());
        }

        builder.append("}");
        data = builder.toString();
        Log.i(TAG, "handleActionCreateRemoteMediaShareTask: request json:" + data);

        HttpResponse httpResponse;

        try {
            httpResponse = FNAS.PostRemoteCall(Util.MEDIASHARE_PARAMETER, data);

            if (httpResponse.getResponseCode() == 200) {

                Log.i(TAG, "insert remote mediashare which source is network succeed");

                mediaShareOperationEvent = new MediaShareOperationEvent(Util.REMOTE_SHARE_CREATED, new OperationSuccess(), mediaShare.cloneMyself());

                RemoteMediaShareJSONObjectParser parser = new RemoteMediaShareJSONObjectParser();

                MediaShare newMediaShare = parser.getRemoteMediaShare(new JSONObject(httpResponse.getResponseData()));

                DBUtils dbUtils = DBUtils.getInstance(this);
                long dbResult = dbUtils.insertRemoteMediaShare(newMediaShare);

                Log.i(TAG, "insert remote mediashare which source is db result:" + dbResult);

                MediaShare mapResult = LocalCache.RemoteMediaShareMapKeyIsUUID.put(newMediaShare.getUuid(), newMediaShare);

                Log.i(TAG, "insert remote mediashare to map result:" + (mapResult != null ? "true" : "false"));

            } else {
                mediaShareOperationEvent = new MediaShareOperationEvent(Util.REMOTE_SHARE_CREATED, new OperationNetworkException(httpResponse.getResponseCode()), mediaShare);

                Log.i(TAG, "insert remote mediashare fail");
            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();

            mediaShareOperationEvent = new MediaShareOperationEvent(Util.REMOTE_SHARE_CREATED, new OperationMalformedUrlException(), mediaShare);

            Log.i(TAG, "insert remote mediashare fail");

        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();

            mediaShareOperationEvent = new MediaShareOperationEvent(Util.REMOTE_SHARE_CREATED, new OperationSocketTimeoutException(), mediaShare);

            Log.i(TAG, "insert remote mediashare fail");
        } catch (IOException ex) {
            ex.printStackTrace();

            mediaShareOperationEvent = new MediaShareOperationEvent(Util.REMOTE_SHARE_CREATED, new OperationIOException(), mediaShare);

            Log.i(TAG, "insert remote mediashare fail");
        } catch (JSONException ex) {
            ex.printStackTrace();

            mediaShareOperationEvent = new MediaShareOperationEvent(Util.REMOTE_SHARE_CREATED, new OperationJSONException(), mediaShare);

            Log.i(TAG, "insert remote mediashare fail");
        }

        EventBus.getDefault().post(mediaShareOperationEvent);

    }

}
