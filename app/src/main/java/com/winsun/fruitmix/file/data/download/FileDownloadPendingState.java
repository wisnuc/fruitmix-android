package com.winsun.fruitmix.file.data.download;

import android.util.Log;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadPendingState extends FileDownloadState {

    public static final String TAG = FileDownloadPendingState.class.getSimpleName();

    public FileDownloadPendingState(FileDownloadItem fileDownloadItem) {
        super(fileDownloadItem);
    }

    @Override
    public DownloadState getDownloadState() {
        return DownloadState.PENDING;
    }

    @Override
    public void startWork() {
        Log.i(TAG, "startWork: it is pending now");
    }

}
