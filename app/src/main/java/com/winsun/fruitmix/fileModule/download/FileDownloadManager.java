package com.winsun.fruitmix.fileModule.download;

import com.winsun.fruitmix.util.FileUtil;

import java.io.File;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2016/11/7.
 */

public enum FileDownloadManager {

    INSTANCE;

    private List<FileDownloadItem> fileDownloadItems;

    private static int FILE_DOWNLOADING_MAX_NUM = 5;

    FileDownloadManager() {
        fileDownloadItems = new ArrayList<>();

    }

    public void addDownloadedFile(FileDownloadItem fileDownloadItem) {

        if (checkIsAlreadyDownloadingStateOrDownloadedState(fileDownloadItem.getFileUUID())) return;

        fileDownloadItems.add(fileDownloadItem);
        fileDownloadItem.setFileDownloadState(new FileDownloadFinishedState(fileDownloadItem));

    }

    public void addFileDownloadItem(FileDownloadItem fileDownloadItem) {

        if (checkIsAlreadyDownloadingStateOrDownloadedState(fileDownloadItem.getFileUUID())) return;

        FileDownloadState fileDownloadState;

        if (checkDownloadingItemIsMax()) {

            fileDownloadState = new FileDownloadPendingState(fileDownloadItem);

        } else {

            fileDownloadState = new FileDownloadingState(fileDownloadItem);
        }

        fileDownloadItem.setFileDownloadState(fileDownloadState);

        fileDownloadItems.add(fileDownloadItem);

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
            if (fileDownloadItem.getDownloadState().equals(DownloadState.DOWNLOADING)) {
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
            if (fileDownloadItem.getFileUUID().equals(fileUUID) && (fileDownloadItem.getDownloadState() == DownloadState.DOWNLOADING || fileDownloadItem.getDownloadState() == DownloadState.FINISHED)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkIsDownloaded(String fileUUID){

        for (FileDownloadItem fileDownloadItem:fileDownloadItems){

            if(fileDownloadItem.getFileUUID().equals(fileUUID) && fileDownloadItem.getDownloadState() == DownloadState.FINISHED){
                return true;
            }
        }

        return false;
    }


    void startPendingDownloadItem() {

        for (FileDownloadItem fileDownloadItem : fileDownloadItems) {
            if (fileDownloadItem.getDownloadState().equals(DownloadState.PENDING)) {
                fileDownloadItem.setFileDownloadState(new FileDownloadingState(fileDownloadItem));
            }
        }
    }

    public List<FileDownloadItem> getFileDownloadItems() {
        return fileDownloadItems;
    }
}
