package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Share;
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
public class LocalShareService extends IntentService {

    public static final String TAG = LocalShareService.class.getSimpleName();

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_LOCAL_SHARE_TASK = "com.winsun.fruitmix.action.local_share_task";

    private DBUtils mDbUtils;
    private List<Share> mShareList;
    private Share mShare;

    private LocalBroadcastManager mManager;

    public LocalShareService() {
        super("LocalShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionLocalShareTask(Context context) {
        Intent intent = new Intent(context, LocalShareService.class);
        intent.setAction(ACTION_LOCAL_SHARE_TASK);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOCAL_SHARE_TASK.equals(action)) {
                try {
                    handleActionLocalShareTask();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionLocalShareTask() throws Exception {
        // TODO: Handle action Foo
        mDbUtils = DBUtils.SINGLE_INSTANCE;
        mManager = LocalBroadcastManager.getInstance(this.getApplicationContext());
        mShareList = mDbUtils.getAllLocalShare();

        Iterator<Share> iterator = mShareList.iterator();
        int shareCount = mShareList.size();

        boolean isContinue = true;

        while (iterator.hasNext()) {

            mShare = iterator.next();

            boolean uploadFileResult = true;
            String[] digests = mShare.getDigest().split(",");
            for (String digest : digests) {
                if (!FNAS.isPhotoInMediaMap(digest)) {

                    if (LocalCache.LocalImagesMap2.containsKey(digest)) {
                        Map<String, String> map = LocalCache.LocalImagesMap2.get(digest);
                        if (!map.containsKey(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS) || map.get(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS).equals("false")) {
                            uploadFileResult = FNAS.UploadFile(map.get("thumb"));
                            Log.i(TAG, "digest:" + digest + "uploadFileResult:" + uploadFileResult);
                            if (!uploadFileResult)
                                break;
                        }
                    }

                }
            }

            // if upload fail,skip this album
            if (!uploadFileResult)
                continue;

            String data, viewers, maintainers;
            int i;

            data = "";
            for (i = 0; i < digests.length; i++) {
                data += ",{\\\"type\\\":\\\"media\\\",\\\"digest\\\":\\\"" + digests[i] + "\\\"}";
            }

            viewers = "";
            for (String key : mShare.getViewer().split(",")) {
                viewers += ",\\\"" + key + "\\\"";
            }
            if (viewers.length() == 0) {
                viewers += ",";
            }
            Log.i(TAG, "winsun viewer:" + viewers);

            maintainers = "";
            for (String key : mShare.getMaintainer().split(",")) {
                maintainers += ",\\\"" + key + "\\\"";
            }

            Log.i(TAG, "winsun maintainers:" + maintainers);

//            data = "{\"album\":true, \"archived\":false,\"maintainers\":\"[" + maintainers.substring(1) + "]\",\"viewers\":\"[" + viewers.substring(1) + "]\",\"tags\":[{\"albumname\":\"" + mShare.getTitle() + "\",\"desc\":\"" + mShare.getDesc() + "\"}],\"contents\":\"[" + data.substring(1) + "]\"}";
//            Log.d(TAG, "winsun old createlocalshare:" + data);

            StringBuilder builder = new StringBuilder();
            builder.append("{\"album\":");
            builder.append(String.valueOf(mShare.isAlbum()));
            builder.append(", \"archived\":false,\"maintainers\":\"[");
            builder.append(maintainers.substring(1));
            builder.append("]\",\"viewers\":\"[");
            builder.append(viewers.substring(1));
            builder.append("]\",\"tags\":[{");
            if (mShare.isAlbum()) {
                builder.append("\"albumname\":\"");
                builder.append(mShare.getTitle());
                builder.append("\",\"desc\":\"");
                builder.append(mShare.getDesc());
                builder.append("\"");
            }
            builder.append("}],\"contents\":\"[");
            builder.append(data.substring(1));
            builder.append("]\"}");

            data = builder.toString();

//            data = "{\"album\":true, \"archived\":false,\"maintainers\":\"[" + maintainers.substring(1) + "]\",\"viewers\":\"[" + viewers.substring(1) + "]\",\"tags\":[{\"albumname\":\"" + mShare.getTitle() + "\",\"desc\":\"" + mShare.getDesc() + "\"}],\"contents\":\"[" + data.substring(1) + "]\"}";
            Log.d(TAG, "winsun createlocalshare:" + data);

            try {
                FNAS.PostRemoteCall("/mediashare", data);
                iterator.remove();

                long result = mDbUtils.deleteLocalShare(mShare.getId());
                LocalCache.DocumentsMap.remove(mShare.getUuid());
                Log.i(TAG, "deleteLocalShare:" + mShare.getId() + "result:" + result);
            } catch (ConnectException ex) {
                isContinue = false;
            } catch (FileNotFoundException ex) {
                isContinue = false;
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        if (shareCount > mShareList.size()) {
            Log.i(TAG, "before send broadcast");
            FNAS.LoadDocuments();
            Intent intent = new Intent(Util.LOCAL_SHARE_CHANGED);
            mManager.sendBroadcast(intent);
            Log.i(TAG, "after send broadcast");
        }

        if (!mShareList.isEmpty() && isContinue) {
            startActionLocalShareTask(this);
        }

    }

}