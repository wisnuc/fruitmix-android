package com.winsun.fruitmix.fileModule.download;

/**
 * Created by Administrator on 2016/11/9.
 */

public class FileDownloadErrorState extends FileDownloadState {

    public FileDownloadErrorState(FileDownloadItem fileDownloadItem) {
        super(fileDownloadItem);
    }

    @Override
    public DownloadState getDownloadState() {
        return DownloadState.ERROR;
    }

    @Override
    public void startWork() {

        FileDownloadManager.INSTANCE.startPendingDownloadItem();
    }
}
