package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ModifyMediaInLocalMediaShareService extends IntentService {

    private static final String TAG = ModifyMediaInLocalMediaShareService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_MODIFY_MEDIA_IN_LOCAL_MEDIASHARE = "com.winsun.fruitmix.services.action.modify_media_in_local_mediashare";

    // TODO: Rename parameters
    private static final String EXTRA_DIFF_CONTENTS_IN_ORIGINAL_MEDIASHARE = "com.winsun.fruitmix.services.extra.diff_contents_in_original_mediashare";
    private static final String EXTRA_DIFF_CONTENTS_MODIFIED_MEDIASHARE = "com.winsun.fruitmix.services.extra.diff_contents_in_modified_mediashare";
    private static final String EXTRA_MODIFIED_MEDIASHARE = "com.winsun.fruitmix.services.extra.modified_mediashare";

    private static MediaShare mDiffContentsOriginalMediaShare;
    private static MediaShare mDiffContentsModifiedMediaShare;
    private static MediaShare mModifiedMediaShare;

    public ModifyMediaInLocalMediaShareService() {
        super("ModifyMediaInLocalMediaShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionModifyMediaInLocalMediaShare(Context context, MediaShare diffContentsOriginalMediaShare, MediaShare diffContentsModifiedMediaShare, MediaShare modifiedMediaShare) {
        Intent intent = new Intent(context, ModifyMediaInLocalMediaShareService.class);
        intent.setAction(ACTION_MODIFY_MEDIA_IN_LOCAL_MEDIASHARE);

        mDiffContentsOriginalMediaShare = diffContentsOriginalMediaShare;
        mDiffContentsModifiedMediaShare = diffContentsModifiedMediaShare;
        mModifiedMediaShare = modifiedMediaShare;

        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MODIFY_MEDIA_IN_LOCAL_MEDIASHARE.equals(action)) {

                handleActionModifyMedia();
            }
        }
    }

    /**
     * Handle action edit photo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionModifyMedia() {

        MediaShare diffContentsInOriginalMediaShare = mDiffContentsOriginalMediaShare;
        MediaShare diffContentsInModifiedMediaShare = mDiffContentsModifiedMediaShare;
        MediaShare modifiedMediaShare = mModifiedMediaShare;

        List<MediaShareContent> differentContentsInOriginalMediaShare = diffContentsInOriginalMediaShare.getMediaShareContents();
        List<MediaShareContent> differentContentsInModifiedMediaShare = diffContentsInModifiedMediaShare.getMediaShareContents();

        OperationEvent operationEvent;

        DBUtils dbUtils = DBUtils.getInstance(this);
        long dbResult = 0;

        for (MediaShareContent mediaShareContent : differentContentsInOriginalMediaShare) {
            dbResult = dbUtils.deleteLocalMediaShareContentByID(mediaShareContent.getId());
        }

        for (MediaShareContent mediaShareContent : differentContentsInModifiedMediaShare) {
            dbResult = dbUtils.insertLocalMediaShareContent(mediaShareContent, modifiedMediaShare.getUuid());
        }

        if (dbResult > 0) {
            Log.i(TAG, "modify media in local mediashare which source is network result:" + dbResult);

            MediaShare mapResult = LocalCache.LocalMediaShareMapKeyIsUUID.put(modifiedMediaShare.getUuid(), modifiedMediaShare);

            Log.i(TAG, "modify media in local mediashare in map result:" + (mapResult != null ? "true" : "false"));

            operationEvent = new OperationEvent(Util.PHOTO_IN_LOCAL_MEDIASHARE_MODIFIED, new OperationSuccess());

        } else {

            operationEvent = new OperationEvent(Util.PHOTO_IN_LOCAL_MEDIASHARE_MODIFIED, new OperationSQLException());
        }

        EventBus.getDefault().post(operationEvent);

    }
}

