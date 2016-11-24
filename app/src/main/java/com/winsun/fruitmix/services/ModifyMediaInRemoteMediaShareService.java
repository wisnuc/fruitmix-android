package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.operationResult.OperationIOException;
import com.winsun.fruitmix.operationResult.OperationJSONException;
import com.winsun.fruitmix.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.operationResult.OperationNetworkException;
import com.winsun.fruitmix.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.operationResult.OperationSuccess;
import com.winsun.fruitmix.operationResult.OperationUploadPhotoFailed;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ModifyMediaInRemoteMediaShareService extends IntentService {

    private static final String TAG = ModifyMediaInLocalMediaShareService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_MODIFY_MEDIA_IN_REMOTE_MEDIASHARE = "com.winsun.fruitmix.services.action.modify_media_in_remote_mediashare";

    // TODO: Rename parameters
    private static final String EXTRA_ORIGINAL_MEDIASHARE = "com.winsun.fruitmix.services.extra.original_mediashare";
    private static final String EXTRA_MODIFIED_MEDIASHARE = "com.winsun.fruitmix.services.extra.modified_mediashare";

    public ModifyMediaInRemoteMediaShareService() {
        super("EditPhotoInMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionEditPhotoInMediaShare(Context context, MediaShare originalMediaShare, MediaShare modifiedMediaShare) {
        Intent intent = new Intent(context, ModifyMediaInRemoteMediaShareService.class);
        intent.setAction(ACTION_MODIFY_MEDIA_IN_REMOTE_MEDIASHARE);
        intent.putExtra(EXTRA_ORIGINAL_MEDIASHARE, originalMediaShare);
        intent.putExtra(EXTRA_MODIFIED_MEDIASHARE, modifiedMediaShare);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MODIFY_MEDIA_IN_REMOTE_MEDIASHARE.equals(action)) {
                MediaShare originalMediaShare = intent.getParcelableExtra(EXTRA_ORIGINAL_MEDIASHARE);
                MediaShare modifiedMediaShare = intent.getParcelableExtra(EXTRA_MODIFIED_MEDIASHARE);
                handleActionModifyMedia(originalMediaShare, modifiedMediaShare);
            }
        }
    }

    /**
     * Handle action edit photo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionModifyMedia(MediaShare originalMediaShare, MediaShare modifiedMediaShare) {

        MediaShare diffContentsOriginalMediaShare = originalMediaShare.cloneMyself();
        diffContentsOriginalMediaShare.clearMediaShareContents();
        diffContentsOriginalMediaShare.initMediaShareContents(originalMediaShare.getDifferentMediaShareContentInCurrentMediaShare(modifiedMediaShare));

        MediaShare diffContentsModifiedMediaShare = modifiedMediaShare.cloneMyself();
        diffContentsModifiedMediaShare.clearMediaShareContents();
        diffContentsModifiedMediaShare.initMediaShareContents(modifiedMediaShare.getDifferentMediaShareContentInCurrentMediaShare(originalMediaShare));

        OperationEvent operationEvent;

        if (!Util.uploadImageDigestsIfNotUpload(this, diffContentsModifiedMediaShare.getMediaDigestInMediaShareContents())) {

            operationEvent = new OperationEvent(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED, new OperationUploadPhotoFailed());

            Log.i(TAG, "edit photo in remote mediashare fail");

        } else {


            String data = getData(diffContentsOriginalMediaShare, diffContentsModifiedMediaShare);

            String req = String.format(getString(R.string.update_mediashare_url), Util.MEDIASHARE_PARAMETER, modifiedMediaShare.getUuid());

            try {
                HttpResponse httpResponse = FNAS.PostRemoteCall(req, data);

                if (httpResponse.getResponseCode() == 200) {

                    operationEvent = new OperationEvent(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED, new OperationSuccess());

                    Log.i(TAG, "modify media in remote mediashare which source is network succeed");

                    DBUtils dbUtils = DBUtils.getInstance(this);

                    long dbResult = 0;

                    for (MediaShareContent mediaShareContent : diffContentsOriginalMediaShare.getMediaShareContents()) {
                        dbResult = dbUtils.deleteRemoteMediaShareContentByID(mediaShareContent.getId());
                    }

                    for (MediaShareContent mediaShareContent : diffContentsModifiedMediaShare.getMediaShareContents()) {
                        dbResult = dbUtils.insertRemoteMediaShareContent(mediaShareContent, modifiedMediaShare.getUuid());
                    }

                    Log.i(TAG, "modify media in remote mediashare which source is network result:" + dbResult);

                    MediaShare mapResult = LocalCache.RemoteMediaShareMapKeyIsUUID.put(modifiedMediaShare.getUuid(), modifiedMediaShare);

                    Log.i(TAG, "modify media in remote mediashare in map result:" + (mapResult != null ? "true" : "false"));
                }else {

                    operationEvent = new OperationEvent(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED, new OperationNetworkException(httpResponse.getResponseCode()));

                }


            } catch (MalformedURLException e) {

                e.printStackTrace();

                operationEvent = new OperationEvent(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED, new OperationMalformedUrlException());

                Log.i(TAG, "edit photo in remote mediashare fail");
            }catch (SocketTimeoutException e) {

                e.printStackTrace();

                operationEvent = new OperationEvent(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED, new OperationSocketTimeoutException());

                Log.i(TAG, "edit photo in remote mediashare fail");
            }catch (IOException e) {

                e.printStackTrace();

                operationEvent = new OperationEvent(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED, new OperationIOException());

                Log.i(TAG, "edit photo in remote mediashare fail");
            }

        }

        EventBus.getDefault().post(operationEvent);

    }

    @NonNull
    private String getData(MediaShare diffContentsOriginalMediaShare, MediaShare diffContentsModifiedMediaShare) {

        String data = "[";
        if(diffContentsOriginalMediaShare.getMediaContentsListSize() != 0){
            data += diffContentsOriginalMediaShare.createStringOperateContentsInMediaShare(Util.DELETE);
        }
        if(diffContentsModifiedMediaShare.getMediaContentsListSize() != 0){
            data += diffContentsModifiedMediaShare.createStringOperateContentsInMediaShare(Util.ADD);
        }
        data += "]";
        return data;

    }

}
