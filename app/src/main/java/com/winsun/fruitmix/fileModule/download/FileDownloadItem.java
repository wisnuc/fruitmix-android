package com.winsun.fruitmix.fileModule.download;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadItem {

    private String fileName;
    private String fileUUID;
    private long fileSize;
    private long fileCurrentDownloadSize;
    private FileDownloadState fileDownloadState;

    public FileDownloadItem(String fileName, long fileSize, String fileUUID) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileUUID = fileUUID;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    void setFileDownloadState(FileDownloadState fileDownloadState) {
        this.fileDownloadState = fileDownloadState;

        fileDownloadState.setFileUUID(fileUUID);
        fileDownloadState.setFileName(fileName);
        fileDownloadState.setFileSize(fileSize);
        fileDownloadState.setFileDownloadItem(this);

        fileDownloadState.notifyDownloadStateChanged();
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

    public DownloadState getDownloadState() {
        return fileDownloadState.getDownloadState();
    }
}
