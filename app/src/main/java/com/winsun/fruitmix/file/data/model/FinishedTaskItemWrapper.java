package com.winsun.fruitmix.file.data.model;

/**
 * Created by Administrator on 2017/8/16.
 */

public class FinishedTaskItemWrapper {

    private String fileUUID;
    private String fileName;

    public FinishedTaskItemWrapper(String fileUUID, String fileName) {
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
