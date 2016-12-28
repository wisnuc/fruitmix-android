package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.MediaShareCommentOperationEvent;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class CreateLocalCommentService extends IntentService {

    private static final String TAG = CreateLocalCommentService.class.getSimpleName();

    private static final String ACTION_CREATE_LOCAL_COMMENT = "com.winsun.fruitmix.services.action.create.local.comment";

    private static final String EXTRA_IMAGE_UUID = "com.winsun.fruitmix.services.extra.image.uuid";
    private static final String EXTRA_COMMENT = "com.winsun.fruitmix.services.extra.comment";

    public CreateLocalCommentService() {
        super("CreateLocalCommentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionCreateLocalComment(Context context, String imageUUID, Comment comment) {
        Intent intent = new Intent(context, CreateLocalCommentService.class);
        intent.setAction(ACTION_CREATE_LOCAL_COMMENT);
        intent.putExtra(EXTRA_IMAGE_UUID, imageUUID);
        intent.putExtra(EXTRA_COMMENT, comment);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_LOCAL_COMMENT.equals(action)) {
                String imageUUID = intent.getStringExtra(EXTRA_IMAGE_UUID);
                Comment comment = intent.getParcelableExtra(EXTRA_COMMENT);
                handleActionCreateLocalComment(imageUUID, comment);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCreateLocalComment(String imageUUID, Comment comment) {

        DBUtils dbUtils = DBUtils.getInstance(this);
        long returnValue = dbUtils.insertLocalComment(comment, imageUUID);

        MediaShareCommentOperationEvent mediaShareCommentOperationEvent;

        if (returnValue > 0) {

            Log.i(TAG, "insert local comment succeed");

            comment.setId(returnValue);

            List<Comment> comments = new ArrayList<>();
            LocalCache.LocalMediaCommentMapKeyIsImageUUID.putIfAbsent(imageUUID, comments);

            comments = LocalCache.LocalMediaCommentMapKeyIsImageUUID.get(imageUUID);

            boolean mapResult = comments.add(comment);

            Log.i(TAG, "insert local media comment to map result:" + mapResult);

            mediaShareCommentOperationEvent = new MediaShareCommentOperationEvent(Util.LOCAL_COMMENT_CREATED, new OperationSuccess(), comment, imageUUID);

        } else {

            Log.i(TAG, "insert local comment fail");

            mediaShareCommentOperationEvent = new MediaShareCommentOperationEvent(Util.LOCAL_COMMENT_CREATED, new OperationSQLException(), comment, imageUUID);
        }
        EventBus.getDefault().post(mediaShareCommentOperationEvent);
    }

}
