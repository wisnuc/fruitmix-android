package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

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

    private DBUtils mDbUtils;
    private Map<String, List<Comment>> mCommentMap;
    private Comment mComment;
    private LocalBroadcastManager mManager;

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
    public static void startActionCreateRemoteCommentTask(Context context) {
        Intent intent = new Intent(context, CreateRemoteCommentService.class);
        intent.setAction(ACTION_CREATE_REMOTE_COMMENT_TASK);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_REMOTE_COMMENT_TASK.equals(action)) {
                handleActionCreateRemoteCommentTask();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCreateRemoteCommentTask() {
        // TODO: Handle action Foo
        mDbUtils = DBUtils.SINGLE_INSTANCE;
        mManager = LocalBroadcastManager.getInstance(this.getApplicationContext());

        mCommentMap = mDbUtils.getAllLocalImageComment();

        int commentCount = mCommentMap.size();

        Iterator<Map.Entry<String, List<Comment>>> iterator = mCommentMap.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<String, List<Comment>> entry = iterator.next();
            String image = entry.getKey();


            if (!FNAS.isPhotoInMediaMap(image)) {

                if (LocalCache.LocalImagesMapKeyIsUUID.containsKey(image)) {
                    String thumb = LocalCache.LocalImagesMapKeyIsUUID.get(image).get("thumb");

                    ConcurrentMap<String, String> map = LocalCache.LocalImagesMapKeyIsThumb.get(thumb);

                    Log.i(TAG, "thumb:" + thumb + "hash:" + image);
                    if (!map.containsKey(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS) || map.get(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS).equals("false")) {
                        // if upload fail,skip this album
                        if (!FNAS.UploadFile(map.get("thumb"))) {
                            continue;
                        } else {
                            map.put(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS, "true");
                            LocalCache.SetGlobalHashMap(Util.LOCAL_IMAGE_MAP_NAME, LocalCache.LocalImagesMapKeyIsThumb);
                            Intent intent = new Intent(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED);
                            mManager.sendBroadcast(intent);
                        }

                    }
                }
            }

            List<Comment> commentList = entry.getValue();
            Iterator<Comment> listIterator = commentList.iterator();
            while (listIterator.hasNext()) {
                mComment = listIterator.next();

                String request = String.format(getString(R.string.photo_comment_url), Util.MEDIA_PARAMETER + "/" + image);
                String data = "{\"shareid\":\"" + mComment.getShareId() + "\", \"text\":\"" + mComment.getText() + "\"}";

                try {
                    String str = FNAS.PostRemoteCall(request, data);
                    if (str != null) {
                        listIterator.remove();
                        mDbUtils.deleteLocalComment(mComment.getId());
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
            if (commentList.size() == 0) {
                iterator.remove();
                mDbUtils.deleteLocalCommentByUUid(image);
            }
        }
        if (commentCount > mCommentMap.size()) {
            Intent intent = new Intent(Util.LOCAL_COMMENT_CHANGED);
            mManager.sendBroadcast(intent);
        }
    }

}
