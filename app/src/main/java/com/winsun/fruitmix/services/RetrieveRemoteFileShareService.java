package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteFileShareParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

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

            HttpResponse remoteFileShareWithMeJSON = FNAS.loadFileSharedWithMe();

            if (remoteFileShareWithMeJSON.getResponseCode() == 200) {
                RemoteDataParser<AbstractRemoteFile> parser = new RemoteFileShareParser();

                LocalCache.RemoteFileShareList.addAll(parser.parse(remoteFileShareWithMeJSON.getResponseData()));

                HttpResponse remoteFileShareWithOthersJSON = FNAS.loadFileShareWithOthers();

                if (remoteFileShareWithOthersJSON.getResponseCode() == 200) {

                    LocalCache.RemoteFileShareList.addAll(parser.parse(remoteFileShareWithOthersJSON.getResponseData()));

                    EventBus.getDefault().post(new OperationEvent(Util.REMOTE_FILE_SHARE_RETRIEVED, new OperationSuccess(R.string.operate)));

                } else {

                    EventBus.getDefault().post(new OperationEvent(Util.REMOTE_FILE_SHARE_RETRIEVED, new OperationNetworkException(remoteFileShareWithOthersJSON.getResponseCode())));
                }

            } else {

                EventBus.getDefault().post(new OperationEvent(Util.REMOTE_FILE_SHARE_RETRIEVED, new OperationNetworkException(remoteFileShareWithMeJSON.getResponseCode())));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();

            EventBus.getDefault().post(new OperationEvent(Util.REMOTE_FILE_SHARE_RETRIEVED, new OperationMalformedUrlException()));
        } catch (SocketTimeoutException e) {
            e.printStackTrace();

            EventBus.getDefault().post(new OperationEvent(Util.REMOTE_FILE_SHARE_RETRIEVED, new OperationSocketTimeoutException()));
        } catch (IOException e) {
            e.printStackTrace();

            EventBus.getDefault().post(new OperationEvent(Util.REMOTE_FILE_SHARE_RETRIEVED, new OperationIOException()));
        } catch (JSONException e) {
            e.printStackTrace();

            EventBus.getDefault().post(new OperationEvent(Util.REMOTE_FILE_SHARE_RETRIEVED, new OperationJSONException()));
        }

    }

}
