package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.MediaShareCommentOperationEvent;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
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
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CreateRemoteCommentService extends IntentService {

    public static final String TAG = CreateRemoteCommentService.class.getSimpleName();

    private static final String ACTION_CREATE_REMOTE_COMMENT_TASK = "com.winsun.fruitmix.services.action.create.remote.comment";

    private static final String EXTRA_COMMMENT = "com.winsun.fruitmix.services.extra.share";
    private static final String EXTRA_MEDIA_UUID = "com.winsun.fruitmix,services.extra,media.uuid";

    public CreateRemoteCommentService() {
        super("CreateRemoteCommentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionCreateRemoteCommentTask(Context context, Comment comment, String mediaUUID) {
        Intent intent = new Intent(context, CreateRemoteCommentService.class);
        intent.setAction(ACTION_CREATE_REMOTE_COMMENT_TASK);
        intent.putExtra(EXTRA_COMMMENT, comment);
        intent.putExtra(EXTRA_MEDIA_UUID, mediaUUID);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_REMOTE_COMMENT_TASK.equals(action)) {
                Comment comment = intent.getParcelableExtra(EXTRA_COMMMENT);
                String mediaUUID = intent.getStringExtra(EXTRA_MEDIA_UUID);
                handleActionCreateRemoteCommentTask(comment, mediaUUID);
            }
        }
    }

    /**
     * Handle action create remote comment in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCreateRemoteCommentTask(Comment comment, String mediaUUID) {

        MediaShareCommentOperationEvent mediaShareCommentOperationEvent;

        String request = String.format(getString(R.string.android_photo_comment_url), Util.MEDIA_PARAMETER + "/" + mediaUUID);
        String data = "{\"shareid\":\"" + comment.getShareId() + "\", \"text\":\"" + comment.getText() + "\"}";

        try {

            HttpResponse httpResponse = FNAS.PostRemoteCall(this,request, data);

            if (httpResponse.getResponseCode() == 200) {

                Log.i(TAG, "insert remote comment which source is network succeed");

                DBUtils dbUtils = DBUtils.getInstance(this);
                long dbResult = dbUtils.insertRemoteComment(comment, mediaUUID);

                Log.i(TAG, "insert remote media comment which source is db result:" + dbResult);

                List<Comment> comments = new ArrayList<>();

                LocalCache.RemoteMediaCommentMapKeyIsImageUUID.putIfAbsent(mediaUUID, comments);

                comments = LocalCache.RemoteMediaCommentMapKeyIsImageUUID.get(mediaUUID);

                boolean mapResult = comments.add(comment);

                Log.i(TAG, "insert remote media comment to map result:" + mapResult);

                mediaShareCommentOperationEvent = new MediaShareCommentOperationEvent(Util.REMOTE_COMMENT_CREATED, new OperationSuccess(R.string.operate), comment, mediaUUID);

            } else {
                mediaShareCommentOperationEvent = new MediaShareCommentOperationEvent(Util.REMOTE_COMMENT_CREATED, new OperationNetworkException(httpResponse.getResponseCode()), comment, mediaUUID);
            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();

            mediaShareCommentOperationEvent = new MediaShareCommentOperationEvent(Util.REMOTE_COMMENT_CREATED, new OperationMalformedUrlException(), comment, mediaUUID);

            Log.i(TAG, "insert remote comment fail");
        }catch (SocketTimeoutException ex) {
            ex.printStackTrace();

            mediaShareCommentOperationEvent = new MediaShareCommentOperationEvent(Util.REMOTE_COMMENT_CREATED, new OperationSocketTimeoutException(), comment, mediaUUID);

            Log.i(TAG, "insert remote comment fail");
        }catch (IOException ex) {
            ex.printStackTrace();

            mediaShareCommentOperationEvent = new MediaShareCommentOperationEvent(Util.REMOTE_COMMENT_CREATED, new OperationIOException(), comment, mediaUUID);

            Log.i(TAG, "insert remote comment fail");
        }

        EventBus.getDefault().post(mediaShareCommentOperationEvent);

    }

}
