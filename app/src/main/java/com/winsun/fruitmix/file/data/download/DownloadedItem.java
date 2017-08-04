package com.winsun.fruitmix.file.data.download;

/**
 * Created by Administrator on 2017/7/28.
 */

public class DownloadedItem {

    private FileDownloadItem fileDownloadItem;
    private long fileTime;
    private String fileCreatorUUID;

    public DownloadedItem(FileDownloadItem fileDownloadItem) {
        this.fileDownloadItem = fileDownloadItem;
    }

    public void setFileTime(long fileTime) {
        this.fileTime = fileTime;
    }

    public long getFileTime() {
        return fileTime;
    }

    public String getFileCreatorUUID() {
        return fileCreatorUUID;
    }

    public void setFileCreatorUUID(String fileCreatorUUID) {
        this.fileCreatorUUID = fileCreatorUUID;
    }

    public String getFileName() {
        return fileDownloadItem.getFileName();
    }

    public long getFileSize() {
        return fileDownloadItem.getFileSize();
    }

    public String getFileUUID() {
        return fileDownloadItem.getFileUUID();
    }

    public FileDownloadItem getFileDownloadItem() {
        return fileDownloadItem;
    }
}
