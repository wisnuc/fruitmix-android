package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.data.download.FinishedTaskItem;
import com.winsun.fruitmix.file.data.download.FileTaskManager;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class RetrieveDownloadedFileService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RETRIEVE_DOWNLOADED_FILE = "com.winsun.fruitmix.services.action.retrieve_downloaded_file";

    public RetrieveDownloadedFileService() {
        super("RetrieveDownloadedFileService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRetrieveDownloadedFile(Context context) {
        Intent intent = new Intent(context, RetrieveDownloadedFileService.class);
        intent.setAction(ACTION_RETRIEVE_DOWNLOADED_FILE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RETRIEVE_DOWNLOADED_FILE.equals(action)) {
                handleActionRetrieveDownloadedFile();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveDownloadedFile() {

        DBUtils dbUtils = DBUtils.getInstance(this);

        FileTaskManager fileTaskManager = FileTaskManager.getInstance();

        List<FinishedTaskItem> downloadItems = dbUtils.getAllCurrentLoginUserDownloadedFile(FNAS.userUUID);

        String[] fileNames = new File(FileUtil.getDownloadFileStoreFolderPath()).list();

        if (fileNames != null && fileNames.length != 0) {

            List<String> fileNameList = Arrays.asList(fileNames);

            Iterator<FinishedTaskItem> itemIterator = downloadItems.iterator();
            while (itemIterator.hasNext()) {
                FinishedTaskItem finishedTaskItem = itemIterator.next();

                if (!fileNameList.contains(finishedTaskItem.getFileName())) {
                    itemIterator.remove();
                    dbUtils.deleteDownloadedFileByUUID(finishedTaskItem.getFileUUID());
                }
            }

        }

        for (FinishedTaskItem finishedTaskItem : downloadItems) {
            fileTaskManager.addFinishedFileTaskItem( finishedTaskItem.getFileTaskItem());
        }

        EventBus.getDefault().post(new OperationEvent(Util.DOWNLOADED_FILE_RETRIEVED, new OperationSuccess(R.string.download)));
    }


}
