package com.winsun.fruitmix.file.data.download;

import android.util.Log;

/**
 * Created by Administrator on 2016/11/14.
 */

public class FileDownloadNoEnoughSpaceState extends FileDownloadState{

    public static final String TAG = FileDownloadNoEnoughSpaceState.class.getSimpleName();

    public FileDownloadNoEnoughSpaceState(FileDownloadItem fileDownloadItem) {
        super(fileDownloadItem);
    }

    @Override
    public DownloadState getDownloadState() {
        return DownloadState.NO_ENOUGH_SPACE;
    }

    @Override
    public void startWork() {
        Log.i(TAG, "startWork: no enough space");
    }
}
