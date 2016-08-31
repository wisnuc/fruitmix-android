package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.winsun.fruitmix.model.Comment;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class CreateLocalCommentService extends IntentService {

    private static final String ACTION_CREATE_LOCAL_COMMENT = "com.winsun.fruitmix.services.action.create.local.comment";

    private static final String EXTRA_IMAGE_UUID = "com.winsun.fruitmix.services.extra.PARAM1";
    private static final String EXTRA_COMMENT = "com.winsun.fruitmix.services.extra.PARAM2";

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
        intent.putExtra(EXTRA_IMAGE_UUID,imageUUID);
        intent.putExtra(EXTRA_COMMENT,comment);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_LOCAL_COMMENT.equals(action)) {
                final String imageUUID = intent.getStringExtra(EXTRA_IMAGE_UUID);
                final Comment comment = intent.getParcelableExtra(EXTRA_COMMENT);
                handleActionFoo(imageUUID, comment);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String imageUUID, Comment comment) {

    }

}
