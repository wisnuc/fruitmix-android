package com.winsun.fruitmix.file.data.download;

/**
 * Created by Administrator on 2017/8/16.
 */

public class DownloadedFileWrapper {

    private String fileUnionKey;
    private String fileName;

    public DownloadedFileWrapper(String fileUnionKey, String fileName) {
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
