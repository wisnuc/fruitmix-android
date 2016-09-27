package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

import java.util.List;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class RetrieveLocalMediaCommentService extends IntentService {
    private static final String ACTION_GET_LOCAL_COMMENT = "com.winsun.fruitmix.services.action.retrieve.local.comment";

    public RetrieveLocalMediaCommentService() {
        super("GetLocalDataService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRetrieveLocalComment(Context context) {
        Intent intent = new Intent(context, RetrieveLocalMediaCommentService.class);
        intent.setAction(ACTION_GET_LOCAL_COMMENT);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_LOCAL_COMMENT.equals(action)) {

                handleActionRetrieveLocalComment();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveLocalComment() {

        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

        Map<String,List<Comment>> localCommentMap = dbUtils.getAllLocalImageCommentKeyIsImageUUID();

        LocalCache.LocalMediaCommentMapKeyIsImageUUID.clear();

        for (Map.Entry<String,List<Comment>> entry:localCommentMap.entrySet()){

            for (Comment comment:entry.getValue()){

                LocalCache.LocalMediaCommentMapKeyIsImageUUID.put(entry.getKey(),comment);
            }
        }

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(Util.LOCAL_MEDIA_COMMENT_RETRIEVED);
        intent.putExtra(Util.OPERATION_RESULT, OperationResult.SUCCEED.name());
        localBroadcastManager.sendBroadcast(intent);

    }

}

