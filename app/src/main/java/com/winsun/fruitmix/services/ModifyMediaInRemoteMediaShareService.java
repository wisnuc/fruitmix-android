package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.operationResult.OperationIOException;
import com.winsun.fruitmix.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.operationResult.OperationNetworkException;
import com.winsun.fruitmix.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

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

    private static final String TAG = ModifyMediaInRemoteMediaShareService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_MODIFY_MEDIA_IN_REMOTE_MEDIASHARE = "com.winsun.fruitmix.services.action.modify_media_in_remote_mediashare";

    // TODO: Rename parameters
    private static final String EXTRA_DIFF_CONTENTS_IN_ORIGINAL_MEDIASHARE = "com.winsun.fruitmix.services.extra.diff_contents_in_original_mediashare";
    private static final String EXTRA_DIFF_CONTENTS_MODIFIED_MEDIASHARE = "com.winsun.fruitmix.services.extra.diff_contents_in_modified_mediashare";
    private static final String EXTRA_MODIFIED_MEDIASHARE = "com.winsun.fruitmix.services.extra.modified_mediashare";

    public ModifyMediaInRemoteMediaShareService() {
        super("ModifyMediaInRemoteMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionModifyMediaInRemoteMediaShare(Context context, MediaShare diffContentsOriginalMediaShare, MediaShare diffContentsModifiedMediaShare, MediaShare modifiedMediaShare) {

        Intent intent = new Intent(context, ModifyMediaInRemoteMediaShareService.class);
        intent.setAction(ACTION_MODIFY_MEDIA_IN_REMOTE_MEDIASHARE);
        intent.putExtra(EXTRA_DIFF_CONTENTS_IN_ORIGINAL_MEDIASHARE, diffContentsOriginalMediaShare);
        intent.putExtra(EXTRA_DIFF_CONTENTS_MODIFIED_MEDIASHARE, diffContentsModifiedMediaShare);
        intent.putExtra(EXTRA_MODIFIED_MEDIASHARE,modifiedMediaShare);
        context.startService(intent);

        Log.i(TAG, "startActionModifyMediaInRemoteMediaShare: start service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MODIFY_MEDIA_IN_REMOTE_MEDIASHARE.equals(action)) {
                MediaShare diffContentsInOriginalMediaShare = intent.getParcelableExtra(EXTRA_DIFF_CONTENTS_IN_ORIGINAL_MEDIASHARE);
                MediaShare diffContentsInModifiedMediaShare = intent.getParcelableExtra(EXTRA_DIFF_CONTENTS_MODIFIED_MEDIASHARE);
                MediaShare modifiedMediaShare = intent.getParcelableExtra(EXTRA_MODIFIED_MEDIASHARE);
                handleActionModifyMedia(diffContentsInOriginalMediaShare, diffContentsInModifiedMediaShare,modifiedMediaShare);
            }
        }
    }

    /**
     * Handle action edit photo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionModifyMedia(MediaShare diffContentsInOriginalMediaShare, MediaShare diffContentsInModifiedMediaShare,MediaShare modifiedMediaShare) {

        OperationEvent operationEvent;

        String data = getData(diffContentsInOriginalMediaShare, diffContentsInModifiedMediaShare);

        String req = String.format(getString(R.string.update_mediashare_url), Util.MEDIASHARE_PARAMETER, diffContentsInModifiedMediaShare.getUuid());

        try {
            HttpResponse httpResponse = FNAS.PostRemoteCall(req, data);

            if (httpResponse.getResponseCode() == 200) {

                operationEvent = new OperationEvent(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED, new OperationSuccess());

                Log.i(TAG, "modify media in remote mediashare which source is network succeed");

                DBUtils dbUtils = DBUtils.getInstance(this);

                long dbResult = 0;

                for (MediaShareContent mediaShareContent : diffContentsInOriginalMediaShare.getMediaShareContents()) {
                    dbResult = dbUtils.deleteRemoteMediaShareContentByID(mediaShareContent.getId());
                }

                for (MediaShareContent mediaShareContent : diffContentsInModifiedMediaShare.getMediaShareContents()) {
                    dbResult = dbUtils.insertRemoteMediaShareContent(mediaShareContent, diffContentsInModifiedMediaShare.getUuid());
                }

                Log.i(TAG, "modify media in remote mediashare which source is network result:" + dbResult);

                MediaShare mapResult = LocalCache.RemoteMediaShareMapKeyIsUUID.put(diffContentsInModifiedMediaShare.getUuid(), modifiedMediaShare);

                Log.i(TAG, "modify media in remote mediashare in map result:" + (mapResult != null ? "true" : "false"));
            } else {

                operationEvent = new OperationEvent(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED, new OperationNetworkException(httpResponse.getResponseCode()));

            }


        } catch (MalformedURLException e) {

            e.printStackTrace();

            operationEvent = new OperationEvent(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED, new OperationMalformedUrlException());

            Log.i(TAG, "edit photo in remote mediashare fail");
        } catch (SocketTimeoutException e) {

            e.printStackTrace();

            operationEvent = new OperationEvent(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED, new OperationSocketTimeoutException());

            Log.i(TAG, "edit photo in remote mediashare fail");
        } catch (IOException e) {

            e.printStackTrace();

            operationEvent = new OperationEvent(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED, new OperationIOException());

            Log.i(TAG, "edit photo in remote mediashare fail");
        }


        EventBus.getDefault().post(operationEvent);

    }

    private String getData(MediaShare diffContentsOriginalMediaShare, MediaShare diffContentsModifiedMediaShare) {

        String data = "[";
        if (diffContentsOriginalMediaShare.getMediaContentsListSize() != 0) {
            data += diffContentsOriginalMediaShare.createStringOperateContentsInMediaShare(Util.DELETE);
        }
        if (diffContentsModifiedMediaShare.getMediaContentsListSize() != 0) {
            data += diffContentsModifiedMediaShare.createStringOperateContentsInMediaShare(Util.ADD);
        }
        data += "]";
        return data;

    }

}
