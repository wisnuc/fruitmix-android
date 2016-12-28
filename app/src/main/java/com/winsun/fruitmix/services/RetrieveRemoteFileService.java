package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.winsun.fruitmix.eventbus.RetrieveFileOperationEvent;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.fileModule.model.RemoteFolder;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteFileFolderParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RetrieveRemoteFileService extends IntentService {

    private static final String ACTION_RETRIEVE_REMOTE_FILE = "com.winsun.fruitmix.services.action.retrieve_remote_file";

    private static final String EXTRA_FOLDER_UUID = "com.winsun.fruitmix.services.extra.folder_uuid";

    public RetrieveRemoteFileService() {
        super("RetrieveRemoteFileService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRetrieveRemoteFile(Context context, String folderUUID) {
        Intent intent = new Intent(context, RetrieveRemoteFileService.class);
        intent.setAction(ACTION_RETRIEVE_REMOTE_FILE);
        intent.putExtra(EXTRA_FOLDER_UUID, folderUUID);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RETRIEVE_REMOTE_FILE.equals(action)) {
                final String folderUUID = intent.getStringExtra(EXTRA_FOLDER_UUID);
                handleActionRetrieveRemoteFile(folderUUID);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveRemoteFile(String folderUUID) {

        try {
            HttpResponse httpResponse = FNAS.loadFileInFolder(folderUUID);

            if(httpResponse.getResponseCode() == 200){

                RemoteFileFolderParser parser = new RemoteFileFolderParser();
                List<AbstractRemoteFile> abstractRemoteFiles = parser.parse(httpResponse.getResponseData());

                LocalCache.RemoteFileMapKeyIsUUID.clear();

                AbstractRemoteFile remoteFolder = new RemoteFolder();
                remoteFolder.setUuid(folderUUID);
                remoteFolder.initChildAbstractRemoteFileList(abstractRemoteFiles);

                LocalCache.RemoteFileMapKeyIsUUID.put(folderUUID,remoteFolder);

                EventBus.getDefault().post(new RetrieveFileOperationEvent(Util.REMOTE_FILE_RETRIEVED, new OperationSuccess(),folderUUID));

            }else {
                EventBus.getDefault().post(new RetrieveFileOperationEvent(Util.REMOTE_FILE_RETRIEVED, new OperationNetworkException(httpResponse.getResponseCode()),folderUUID));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();

            EventBus.getDefault().post(new RetrieveFileOperationEvent(Util.REMOTE_FILE_RETRIEVED, new OperationMalformedUrlException(),folderUUID));
        }catch (SocketTimeoutException e) {
            e.printStackTrace();

            EventBus.getDefault().post(new RetrieveFileOperationEvent(Util.REMOTE_FILE_RETRIEVED, new OperationSocketTimeoutException(),folderUUID));
        }catch (IOException e) {
            e.printStackTrace();

            EventBus.getDefault().post(new RetrieveFileOperationEvent(Util.REMOTE_FILE_RETRIEVED, new OperationIOException(),folderUUID));
        }catch (JSONException e) {
            e.printStackTrace();

            EventBus.getDefault().post(new RetrieveFileOperationEvent(Util.REMOTE_FILE_RETRIEVED, new OperationJSONException(),folderUUID));
        }

    }

}
