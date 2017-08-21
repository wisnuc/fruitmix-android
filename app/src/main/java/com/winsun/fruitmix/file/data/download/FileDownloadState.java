package com.winsun.fruitmix.file.data.download;

import com.winsun.fruitmix.eventbus.DownloadStateChangedEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/11/7.
 */

public abstract class FileDownloadState {

    private String fileName;
    private String fileUUID;
    private FileDownloadItem fileDownloadItem;

    public FileDownloadState(FileDownloadItem fileDownloadItem) {
        this.fileDownloadItem = fileDownloadItem;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileUUID(String fileUUID) {
        this.fileUUID = fileUUID;
    }

    public void setFileSize(long fileSize) {
        fileDownloadItem.setFileSize(fileSize);
    }

    public void setFileCurrentDownloadSize(long fileCurrentDownloadSize) {
        fileDownloadItem.setFileCurrentDownloadSize(fileCurrentDownloadSize);
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUUID() {
        return fileUUID;
    }

    public String getParentFolderUUID() {
        return fileDownloadItem.getParentFolderUUID();
    }

    public String getDriveUUID(){
        return fileDownloadItem.getDriveUUID();
    }

    public FileDownloadItem getFileDownloadItem() {
        return fileDownloadItem;
    }

    public abstract DownloadState getDownloadState();

    public abstract void startWork();

    public void notifyDownloadStateChanged() {

        EventBus.getDefault().postSticky(new DownloadStateChangedEvent(getDownloadState()));
    }

}
