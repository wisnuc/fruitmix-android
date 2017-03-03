package com.winsun.fruitmix.fileModule.download;

import com.winsun.fruitmix.refactor.business.callback.FileDownloadOperationCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadItem {

    private String fileName;
    private String fileUUID;
    private long fileSize;
    private long fileTime;
    private long fileCurrentDownloadSize;
    private FileDownloadState fileDownloadState;

    private List<FileDownloadOperationCallback.FileDownloadStateChangedCallback> callbacks;

    private FileDownloadOperationCallback.StartDownloadFileCallback startDownloadFileCallback;

    public FileDownloadItem(String fileName, long fileSize, String fileUUID) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileUUID = fileUUID;

        callbacks = new ArrayList<>();
    }

    public void setFileDownloadState(FileDownloadState fileDownloadState) {
        this.fileDownloadState = fileDownloadState;

        fileDownloadState.setFileUUID(fileUUID);
        fileDownloadState.setFileName(fileName);
        fileDownloadState.setFileSize(fileSize);

        fileDownloadState.startWork();

        fileDownloadState.notifyDownloadStateChanged();
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

    public FileDownloadState getFileDownloadState() {
        return fileDownloadState;
    }

    public String getFileUUID() {
        return fileUUID;
    }

    public void setFileTime(long fileTime) {
        this.fileTime = fileTime;
    }

    public long getFileTime() {
        return fileTime;
    }

    public void registerStateChangedCallback(FileDownloadOperationCallback.FileDownloadStateChangedCallback callback) {
        callbacks.add(callback);
    }

    public void unregisterStateChangedCallback(FileDownloadOperationCallback.FileDownloadStateChangedCallback callback) {
        callbacks.remove(callback);
    }

    public void startDownload() {
        startDownloadFileCallback.start();
    }

    public void registerStartDownloadFileCallback(FileDownloadOperationCallback.StartDownloadFileCallback startDownloadFileCallback) {
        this.startDownloadFileCallback = startDownloadFileCallback;
    }

    public void notifyDownloadStateChanged(DownloadState downloadState) {
        for (FileDownloadOperationCallback.FileDownloadStateChangedCallback callback : callbacks) {
            callback.onStateChanged(downloadState);
        }
    }
}
