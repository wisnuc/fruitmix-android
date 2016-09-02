package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

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

    private DBUtils mDbUtils;
    private List<MediaShare> mMediaShareList;
    private MediaShare mMediaShare;

    private LocalBroadcastManager mManager;

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
    public static void startActionCreateRemoteMediaShareTask(Context context) {
        Intent intent = new Intent(context, CreateRemoteMediaShareService.class);
        intent.setAction(ACTION_CREATE_REMOTE_MEDIASHARE_TASK);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_REMOTE_MEDIASHARE_TASK.equals(action)) {
                handleActionCreateRemoteMediaShareTask();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCreateRemoteMediaShareTask() {
        // TODO: Handle action Foo
        mDbUtils = DBUtils.SINGLE_INSTANCE;
        mManager = LocalBroadcastManager.getInstance(this.getApplicationContext());
        mMediaShareList = mDbUtils.getAllLocalShare();

        Log.i(TAG, "local share:" + mMediaShareList);

        Iterator<MediaShare> iterator = mMediaShareList.iterator();
        int shareCount = mMediaShareList.size();

        while (iterator.hasNext()) {

            mMediaShare = iterator.next();

            boolean uploadFileResult = true;
            String[] digests = (String[]) mMediaShare.getImageDigests().toArray();
            int uploadSucceedCount = 0;

            for (String digest : digests) {
                if (!FNAS.isPhotoInMediaMap(digest)) {

                    if (LocalCache.LocalImagesMapKeyIsUUID.containsKey(digest)) {
                        String thumb = LocalCache.LocalImagesMapKeyIsUUID.get(digest).get("thumb");

                        ConcurrentMap<String, String> map = LocalCache.LocalImagesMapKeyIsThumb.get(thumb);

                        Log.i(TAG, "thumb:" + thumb + "hash:" + digest);

                        if (!map.containsKey(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS) || map.get(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS).equals("false")) {
                            uploadFileResult = FNAS.UploadFile(thumb);

                            map.put(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS, String.valueOf(uploadFileResult));
                            Log.i(TAG, "digest:" + digest + "uploadFileResult:" + uploadFileResult);

                            if (!uploadFileResult) {
                                break;
                            }else {
                                uploadSucceedCount++;
                            }
                        }
                    }

                }
            }

            if (uploadSucceedCount > 0) {
                LocalCache.SetGlobalHashMap(Util.LOCAL_IMAGE_MAP_NAME, LocalCache.LocalImagesMapKeyIsThumb);
                Intent intent = new Intent(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED);
                mManager.sendBroadcast(intent);
            }

            // if upload fail,skip this album,otherwise save upload state
            if (!uploadFileResult) {
                continue;
            }

            String data, viewers, maintainers;
            int i;

            data = "";
            for (i = 0; i < digests.length; i++) {
                data += ",{\\\"type\\\":\\\"media\\\",\\\"digest\\\":\\\"" + digests[i] + "\\\"}";
            }

            viewers = "";
            for (String key : mMediaShare.getViewer()) {
                viewers += ",\\\"" + key + "\\\"";
            }
            if (viewers.length() == 0) {
                viewers += ",";
            }
            Log.i(TAG, "winsun viewer:" + viewers);

            maintainers = "";
            for (String key : mMediaShare.getMaintainer()) {
                maintainers += ",\\\"" + key + "\\\"";
            }

            Log.i(TAG, "winsun maintainers:" + maintainers);

            StringBuilder builder = new StringBuilder();
            builder.append("{\"album\":");
            builder.append(String.valueOf(mMediaShare.isAlbum()));
            builder.append(", \"archived\":false,\"maintainers\":\"[");
            builder.append(maintainers.substring(1));
            builder.append("]\",\"viewers\":\"[");
            builder.append(viewers.substring(1));
            builder.append("]\",\"tags\":[{");
            if (mMediaShare.isAlbum()) {
                builder.append("\"albumname\":\"");
                builder.append(mMediaShare.getTitle());
                builder.append("\",\"desc\":\"");
                builder.append(mMediaShare.getDesc());
                builder.append("\"");
            }
            builder.append("}],\"contents\":\"[");
            builder.append(data.substring(1));
            builder.append("]\"}");

            data = builder.toString();

            Log.d(TAG, "winsun create local share:" + data);

            try {
                String str = FNAS.PostRemoteCall(Util.MEDIASHARE_PARAMETER, data);
                if (str != null) {
                    iterator.remove();
                    long deleteResult = mDbUtils.deleteLocalShare(mMediaShare.getId());
                    LocalCache.SharesMap.remove(mMediaShare.getUuid());
                    Log.i(TAG, "deleteLocalShare:" + mMediaShare.getId() + "result:" + deleteResult);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        if (shareCount > mMediaShareList.size()) {
            Log.i(TAG, "before send broadcast");
            FNAS.retrieveShareMap();
            Intent intent = new Intent(Util.LOCAL_SHARE_CHANGED);
            mManager.sendBroadcast(intent);
            Log.i(TAG, "after send broadcast");
        }

    }

}
