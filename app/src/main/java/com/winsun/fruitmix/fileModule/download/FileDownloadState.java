package com.winsun.fruitmix.fileModule.download;

import com.winsun.fruitmix.eventbus.DownloadEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/11/7.
 */

public abstract class FileDownloadState {

    private String fileName;
    private String fileUUID;
    private long fileSize;
    private long fileCurrentDownloadSize;
    private FileDownloadItem fileDownloadItem;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileUUID(String fileUUID) {
        this.fileUUID = fileUUID;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileCurrentDownloadSize(long fileCurrentDownloadSize) {
        this.fileCurrentDownloadSize = fileCurrentDownloadSize;
    }

    public void setFileDownloadItem(FileDownloadItem fileDownloadItem) {
        this.fileDownloadItem = fileDownloadItem;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUUID() {
        return fileUUID;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getFileCurrentDownloadSize() {
        return fileCurrentDownloadSize;
    }

    public FileDownloadItem getFileDownloadItem() {
        return fileDownloadItem;
    }

    public abstract DownloadState getDownloadState();

    public void notifyDownloadStateChanged() {

        EventBus.getDefault().post(new DownloadEvent());

    }

}
