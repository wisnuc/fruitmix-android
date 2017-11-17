package com.winsun.fruitmix.file.data.download;

import android.util.Log;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadingState extends FileDownloadState {

    public static final String TAG = FileDownloadingState.class.getSimpleName();

    public FileDownloadingState(FileDownloadItem fileDownloadItem) {
        super(fileDownloadItem);
    }

    @Override
    public TaskState getDownloadState() {
        return TaskState.DOWNLOADING_OR_UPLOADING;
    }

    @Override
    public void startWork() {
        Log.d(TAG, "startWork: ");
    }

}
