package com.winsun.fruitmix.file.data.download;

import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadManager {

    private static final String TAG = FileDownloadManager.class.getSimpleName();

    private static FileDownloadManager instance;

    private List<FileDownloadItem> fileDownloadItems;

    private static int FILE_DOWNLOADING_MAX_NUM = 3;

    private FileDownloadManager() {
        fileDownloadItems = new ArrayList<>();
    }

    public static FileDownloadManager getInstance() {

        if (instance == null)
            instance = new FileDownloadManager();

        return instance;
    }

    public void addDownloadedFile(FileDownloadItem fileDownloadItem) {

        if (checkIsAlreadyDownloadingStateOrDownloadedState(fileDownloadItem.getFileUUID())) return;

        fileDownloadItems.add(fileDownloadItem);
        fileDownloadItem.setFileDownloadState(new FileDownloadFinishedState(fileDownloadItem));

    }

    public void addFileDownloadItem(FileDownloadItem fileDownloadItem, StationFileRepository stationFileRepository,String currentUserUUID) {

        if (checkIsAlreadyDownloadingStateOrDownloadedState(fileDownloadItem.getFileUUID())) return;

        FileDownloadState fileDownloadState;

        if (checkDownloadingItemIsMax()) {

            fileDownloadState = new FileDownloadPendingState(fileDownloadItem,stationFileRepository,currentUserUUID);

        } else {

            fileDownloadState = new FileStartDownloadState(fileDownloadItem, stationFileRepository, ThreadManagerImpl.getInstance(),currentUserUUID);
        }

        // must first add and then set,because setFileDownloadState will call notifyDownloadStateChanged,update ui using fileDownloadItems

        fileDownloadItems.add(fileDownloadItem);

        fileDownloadItem.setFileDownloadState(fileDownloadState);

        Log.d(TAG, "handleEvent: addFileDownloadItem: " + getFileDownloadItems());

    }

    public void deleteFileDownloadItem(List<String> fileUUIDs) {

        for (String fileUUID : fileUUIDs) {

            Iterator<FileDownloadItem> itemIterator = fileDownloadItems.iterator();
            while (itemIterator.hasNext()) {
                FileDownloadItem fileDownloadItem = itemIterator.next();
                if (fileDownloadItem.getFileUUID().equals(fileUUID))
                    itemIterator.remove();
            }

        }

    }

    private boolean checkDownloadingItemIsMax() {

        int downloadingItem = 0;

        for (FileDownloadItem fileDownloadItem : fileDownloadItems) {
            if (fileDownloadItem.getDownloadState().equals(DownloadState.DOWNLOADING) || fileDownloadItem.getDownloadState().equals(DownloadState.START_DOWNLOAD)) {
                downloadingItem++;
            }
            if (downloadingItem == FILE_DOWNLOADING_MAX_NUM) {
                return true;
            }
        }

        return false;

    }

    private boolean checkIsAlreadyDownloadingStateOrDownloadedState(String fileUUID) {

        List<FileDownloadItem> fileDownloadItems = getFileDownloadItems();

        for (FileDownloadItem fileDownloadItem : fileDownloadItems) {
            if (fileDownloadItem.getFileUUID().equals(fileUUID) && (fileDownloadItem.getDownloadState() == DownloadState.START_DOWNLOAD || fileDownloadItem.getDownloadState() == DownloadState.DOWNLOADING || fileDownloadItem.getDownloadState() == DownloadState.FINISHED)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkIsDownloaded(String fileUUID) {

        for (FileDownloadItem fileDownloadItem : fileDownloadItems) {

            if (fileDownloadItem.getFileUUID().equals(fileUUID) && fileDownloadItem.getDownloadState() == DownloadState.FINISHED) {
                return true;
            }
        }

        return false;
    }


    void startPendingDownloadItem() {

        for (FileDownloadItem fileDownloadItem : fileDownloadItems) {

            if (!checkDownloadingItemIsMax() && fileDownloadItem.getDownloadState().equals(DownloadState.PENDING)) {

                FileDownloadPendingState state = (FileDownloadPendingState) fileDownloadItem.getFileDownloadState();

                Log.d(TAG, "startPendingDownloadItem: " + fileDownloadItem.getFileName());

                fileDownloadItem.setFileDownloadState(new FileStartDownloadState(fileDownloadItem, state.getStationFileRepository(),ThreadManagerImpl.getInstance(), state.getCurrentUserUUID()));
            }
        }
    }

    public List<FileDownloadItem> getFileDownloadItems() {

        return Collections.unmodifiableList(fileDownloadItems);
    }

    public void clearFileDownloadItems() {
        if (fileDownloadItems != null) {
            fileDownloadItems.clear();
        }
    }
}
