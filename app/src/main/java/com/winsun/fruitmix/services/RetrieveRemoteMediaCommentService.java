package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteMediaCommentParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RetrieveRemoteMediaCommentService extends IntentService {

    private static final String TAG = RetrieveRemoteMediaCommentService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RETRIEVE_REMOTE_MEDIA_COMMENT = "com.winsun.fruitmix.services.action.retrieve_remote_media_comment";

    private static String EXTRA_MEDIA_UUID = "com.winsun.fruitmix.services.extra_media_uuid";

    public RetrieveRemoteMediaCommentService() {
        super("RetrieveRemoteMediaCommentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRetrieveRemoteMediaComment(Context context, String mediaUUID) {
        Intent intent = new Intent(context, RetrieveRemoteMediaCommentService.class);
        intent.setAction(ACTION_RETRIEVE_REMOTE_MEDIA_COMMENT);
        intent.putExtra(EXTRA_MEDIA_UUID, mediaUUID);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RETRIEVE_REMOTE_MEDIA_COMMENT.equals(action)) {
                String mediaUUID = intent.getStringExtra(EXTRA_MEDIA_UUID);
                handleActionRetrieveRemoteMediaComment(mediaUUID);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveRemoteMediaComment(String mediaUUID) {

        List<Comment> comments;

        DBUtils dbUtils = DBUtils.getInstance(this);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

        try {

            String json = FNAS.loadRemoteMediaComment(this, mediaUUID);

            RemoteDataParser<Comment> parser = new RemoteMediaCommentParser();
            comments = parser.parse(json);

            dbUtils.deleteRemoteCommentByUUid(mediaUUID);

            for (Comment comment : comments) {
                dbUtils.insertRemoteComment(comment, mediaUUID);
            }

            LocalCache.RemoteMediaCommentMapKeyIsImageUUID.remove(mediaUUID);

            LocalCache.RemoteMediaCommentMapKeyIsImageUUID.putIfAbsent(mediaUUID,comments);

            Log.i(TAG, "retrieve remote media comment from network");

            Intent intent = new Intent(Util.REMOTE_MEDIA_COMMENT_RETRIEVED);
            intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.SUCCEED.name());
            localBroadcastManager.sendBroadcast(intent);

        } catch (Exception e) {
            e.printStackTrace();

            comments = dbUtils.getRemoteImageCommentByUUid(mediaUUID);

            LocalCache.RemoteMediaCommentMapKeyIsImageUUID.remove(mediaUUID);

            LocalCache.RemoteMediaCommentMapKeyIsImageUUID.putIfAbsent(mediaUUID,comments);

            Log.i(TAG, "retrieve remote media comment from db");

            Intent intent = new Intent(Util.REMOTE_MEDIA_COMMENT_RETRIEVED);
            intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.SUCCEED.name());
            localBroadcastManager.sendBroadcast(intent);
        }


    }

}
