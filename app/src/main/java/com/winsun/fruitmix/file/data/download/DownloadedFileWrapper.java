package com.winsun.fruitmix.file.data.download;

/**
 * Created by Administrator on 2017/8/16.
 */

public class DownloadedFileWrapper {

    private String fileUUID;
    private String fileName;

    public DownloadedFileWrapper(String fileUUID, String fileName) {
        this.fileUUID = fileUUID;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUUID() {
        return fileUUID;
    }
}
