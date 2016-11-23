package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteFileShareParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class RetrieveRemoteFileShareService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RETRIEVE_REMOTE_FILE_SHARE = "com.winsun.fruitmix.services.action.retrieve_remote_file_share";


    public RetrieveRemoteFileShareService() {
        super("RetrieveRemoteFileShareService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRetrieveRemoteFileShare(Context context) {
        Intent intent = new Intent(context, RetrieveRemoteFileShareService.class);
        intent.setAction(ACTION_RETRIEVE_REMOTE_FILE_SHARE);

        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RETRIEVE_REMOTE_FILE_SHARE.equals(action)) {
                handleActionRetrieveRemoteFileShare();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveRemoteFileShare() {

        try {

            LocalCache.RemoteFileShareList.clear();

            String remoteFileShareWithMeJSON = FNAS.loadFileSharedWithMe();

            RemoteDataParser<AbstractRemoteFile> parser = new RemoteFileShareParser();

            LocalCache.RemoteFileShareList.addAll(parser.parse(remoteFileShareWithMeJSON));

            String remoteFileShareWithOthersJSON = FNAS.loadFileShareWithOthers();

            LocalCache.RemoteFileShareList.addAll(parser.parse(remoteFileShareWithOthersJSON));

            EventBus.getDefault().post(new OperationEvent(Util.REMOTE_FILE_SHARE_RETRIEVED, OperationResultType.SUCCEED));

        } catch (Exception e) {
            e.printStackTrace();

            EventBus.getDefault().post(new OperationEvent(Util.REMOTE_FILE_SHARE_RETRIEVED, OperationResultType.FAIL));
        }


    }

}
