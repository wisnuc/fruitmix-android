package com.winsun.fruitmix.fileModule.download;

import java.util.ArrayList;

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

    public void handleDownloadItemFinished() {

        startPendingDownloadItem();
    }


    public void addFileDownloadItem(FileDownloadItem fileDownloadItem) {

        FileDownloadState fileDownloadState;

        if (checkDownloadingItemIsMax()) {

            fileDownloadState = new FileDownloadPendingState(fileDownloadItem);

        } else {

            fileDownloadState = new FileDownloadingState(fileDownloadItem);
        }

        fileDownloadItem.setFileDownloadState(fileDownloadState);

        fileDownloadItems.add(fileDownloadItem);

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

    private void startPendingDownloadItem() {

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
