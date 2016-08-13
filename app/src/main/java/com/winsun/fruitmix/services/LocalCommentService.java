package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LocalCommentService extends IntentService {

    public static final String TAG = LocalCommentService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_LOCAL_COMMENT_TASK = "com.winsun.fruitmix.services.action.local_comment_task";

    private DBUtils mDbUtils;
    private Map<String, List<Comment>> mCommentMap;
    private Comment mComment;
    private LocalBroadcastManager mManager;


    public LocalCommentService() {
        super("LocalCommentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionLocalCommentTask(Context context) {
        Intent intent = new Intent(context, LocalCommentService.class);
        intent.setAction(ACTION_LOCAL_COMMENT_TASK);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOCAL_COMMENT_TASK.equals(action)) {
                try {
                    handleActionLocalCommentTask();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionLocalCommentTask() throws Exception {
        // TODO: Handle action Foo
        mDbUtils = DBUtils.SINGLE_INSTANCE;
        mManager = LocalBroadcastManager.getInstance(this.getApplicationContext());

        mCommentMap = mDbUtils.getAllLocalImageComment();

        int albumCount = mCommentMap.size();

        Iterator<Map.Entry<String, List<Comment>>> iterator = mCommentMap.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<String, List<Comment>> entry = iterator.next();
            String image = entry.getKey();

            if (!FNAS.isPhotoInMediaMap(image)) {

                if (LocalCache.LocalImagesMap2.containsKey(image)) {
                    Map<String, String> map = LocalCache.LocalImagesMap2.get(image);
                    if (!map.containsKey(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS) || map.get(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS).equals("false")) {
                        // if upload fail,skip this album
                        if (!FNAS.UploadFile(map.get("thumb")))
                            continue;
                    }
                }

            }

            List<Comment> commentList = entry.getValue();
            Iterator<Comment> listIterator = commentList.iterator();
            while (listIterator.hasNext()) {
                mComment = listIterator.next();

                String request = "/media/" + image + "?type=comments";
                String data = "{\"shareid\":\"" + mComment.getShareId() + "\", \"text\":\"" + mComment.getText() + "\"}";

                try {
                    FNAS.PostRemoteCall(request, data);
                    listIterator.remove();
                    mDbUtils.deleteLocalComment(mComment.getId());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
            if (commentList.size() == 0) {
                iterator.remove();
            }
        }
        if (albumCount > mCommentMap.size()) {
            FNAS.LoadDocuments();
            Intent intent = new Intent(Util.LOCAL_COMMENT_CHANGED);
            mManager.sendBroadcast(intent);
        }
    }

}
