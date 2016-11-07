package com.winsun.fruitmix.fileModule.download;

import com.winsun.fruitmix.eventbus.DownloadEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
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

        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    private void handleDownloadItemFinished(DownloadEvent downloadEvent) {

        startPendingDownloadItem();
    }


    public void addFileDownloadItem(FileDownloadItem fileDownloadItem) {

        FileDownloadState fileDownloadState;

        fileDownloadItems.add(fileDownloadItem);

        if (checkDownloadingItemIsMax()) {

            fileDownloadState = new FileDownloadPendingState();

        } else {

            fileDownloadState = new FileDownloadingState();
        }

        fileDownloadItem.setFileDownloadState(fileDownloadState);

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
                fileDownloadItem.setFileDownloadState(new FileDownloadingState());
            }
        }
    }

    public List<FileDownloadItem> getFileDownloadItems() {
        return fileDownloadItems;
    }
}
