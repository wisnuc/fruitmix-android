package com.winsun.fruitmix.file.data.model;

/**
 * Created by Administrator on 2017/8/16.
 */

public class FinishedTaskItemWrapper {

    private String fileUnionKey;
    private String fileName;

    public FinishedTaskItemWrapper(String fileUnionKey, String fileName) {
        this.fileUnionKey = fileUnionKey;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUnionKey() {
        return fileUnionKey;
    }

}
