package com.winsun.fruitmix.fileModule.download;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

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
