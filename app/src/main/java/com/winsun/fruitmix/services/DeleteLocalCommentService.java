package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.MediaShareCommentOperationEvent;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.Iterator;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DeleteLocalCommentService extends IntentService {

    private static final String TAG = DeleteLocalCommentService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_DELETE_LOCAL_COMMENT = "com.winsun.fruitmix.services.action.delete_local_comment";

    // TODO: Rename parameters
    private static final String EXTRA_COMMENT = "com.winsun.fruitmix.services.extra.comment";
    private static final String EXTRA_IMAGE_UUID = "com.winsun.fruitmix.services.extra.image.uuid";


    public DeleteLocalCommentService() {
        super("DeleteLocalCommentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionDeleteLocalComment(Context context, Comment comment, String imageUUID) {
        Intent intent = new Intent(context, DeleteLocalCommentService.class);
        intent.setAction(ACTION_DELETE_LOCAL_COMMENT);
        intent.putExtra(EXTRA_COMMENT, comment);
        intent.putExtra(EXTRA_IMAGE_UUID, imageUUID);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DELETE_LOCAL_COMMENT.equals(action)) {
                Comment comment = intent.getParcelableExtra(EXTRA_COMMENT);
                String imageUUID = intent.getStringExtra(EXTRA_IMAGE_UUID);
                handleActionDeleteLocalComment(comment, imageUUID);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDeleteLocalComment(Comment comment, String imageUUID) {
        // TODO: Handle action Foo

        DBUtils dbUtils = DBUtils.getInstance(this);

        MediaShareCommentOperationEvent mediaShareCommentOperationEvent;

        long returnValue = dbUtils.deleteLocalComment(comment.getId());

        if (returnValue > 0) {

            Log.i(TAG, "delete local comment in db succeed");

            boolean result = false;
            for (List<Comment> comments : LocalCache.LocalMediaCommentMapKeyIsImageUUID.values()) {
                Iterator<Comment> iterator = comments.iterator();
                while (iterator.hasNext()) {
                    Comment comment1 = iterator.next();
                    if (comment1.getId() == comment.getId()) {
                        iterator.remove();
                        result = true;
                    }

                }

            }

            Log.i(TAG, "delete local comment in map result:" + result);

            mediaShareCommentOperationEvent = new MediaShareCommentOperationEvent(Util.LOCAL_COMMENT_DELETED, new OperationSuccess(R.string.operate), comment, imageUUID);

        } else {

            Log.i(TAG, "delete local comment fail");

            mediaShareCommentOperationEvent = new MediaShareCommentOperationEvent(Util.LOCAL_COMMENT_DELETED, new OperationSQLException(), comment, imageUUID);
        }

        EventBus.getDefault().post(mediaShareCommentOperationEvent);
    }

}
