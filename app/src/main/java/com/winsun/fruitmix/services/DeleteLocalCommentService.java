package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

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

        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

        long returnValue = dbUtils.deleteLocalComment(comment.getId());

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(Util.LOCAL_COMMENT_DELETED);

        if (returnValue > 0) {

            Log.i(TAG, "delete local comment in db succeed");

            boolean result = false;
            for (Comment comment1 : LocalCache.LocalMediaCommentMapKeyIsImageUUID.values()) {
                if (comment.getId() == comment1.getId()) {
                    result = LocalCache.LocalMediaCommentMapKeyIsImageUUID.remove(imageUUID, comment1);
                }
            }

            Log.i(TAG, "delete local comment in map result:" + result);

            intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.SUCCEED.name());
            intent.putExtra(Util.OPERATION_IMAGE_UUID, imageUUID);
            intent.putExtra(Util.OPERATION_COMMENT, comment);

        } else {

            Log.i(TAG, "delete local comment fail");

            intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.FAIL.name());

        }

        broadcastManager.sendBroadcast(intent);
    }

}
