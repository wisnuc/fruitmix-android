package com.winsun.fruitmix.file.data.download;

import android.util.Log;

import java.util.concurrent.Future;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadItem {

    public static final String TAG = FileDownloadItem.class.getSimpleName();

    private String fileName;
    private String fileUUID;
    private long fileSize;

    private long fileCurrentDownloadSize;

    private FileDownloadState fileDownloadState;

    private String parentFolderUUID;

    private Future<Boolean> future;

    public FileDownloadItem(String fileName, long fileSize, String fileUUID) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileUUID = fileUUID;
    }

    public FileDownloadItem(String fileName, long fileSize, String fileUUID, String parentFolderUUID) {
        this.fileName = fileName;
        this.fileUUID = fileUUID;
        this.fileSize = fileSize;
        this.parentFolderUUID = parentFolderUUID;
    }

    public void setFileDownloadState(FileDownloadState fileDownloadState) {
        this.fileDownloadState = fileDownloadState;

        fileDownloadState.setFileUUID(fileUUID);
        fileDownloadState.setFileName(fileName);
        fileDownloadState.setFileSize(fileSize);

        fileDownloadState.startWork();

        fileDownloadState.notifyDownloadStateChanged();
    }

    public FileDownloadState getFileDownloadState() {
        return fileDownloadState;
    }

    void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    void setFileCurrentDownloadSize(long fileCurrentDownloadSize) {
        this.fileCurrentDownloadSize = fileCurrentDownloadSize;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getFileCurrentDownloadSize() {
        return fileCurrentDownloadSize;
    }

    public DownloadState getDownloadState() {
        return fileDownloadState.getDownloadState();
    }

    public String getFileUUID() {
        return fileUUID;
    }

    public String getParentFolderUUID() {
        return parentFolderUUID;
    }

    public void setFuture(Future<Boolean> future) {
        this.future = future;
    }

    public void cancelDownloadItem() {
        if (future != null)
            future.cancel(true);
    }

    public int getCurrentProgress(int max) {
        Log.d(TAG, "refreshView: currentDownloadSize:" + getFileCurrentDownloadSize() + " fileSize:" + getFileSize());

        float currentProgress = getFileCurrentDownloadSize() * max / getFileSize();

        Log.d(TAG, "refreshView: currentProgress:" + currentProgress);

        return (int) currentProgress;
    }
}
