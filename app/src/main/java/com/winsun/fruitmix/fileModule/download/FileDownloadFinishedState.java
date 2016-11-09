package com.winsun.fruitmix.fileModule.download;

import android.util.Log;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadFinishedState extends FileDownloadState {

    public static final String TAG = FileDownloadFinishedState.class.getSimpleName();

    public FileDownloadFinishedState(FileDownloadItem fileDownloadItem) {
        super(fileDownloadItem);
    }

    @Override
    public DownloadState getDownloadState() {
        return DownloadState.FINISHED;
    }

    @Override
    public void startWork() {

        FileDownloadManager.INSTANCE.startPendingDownloadItem();

    }

}
