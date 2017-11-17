package com.winsun.fruitmix.file.data.download;

import com.winsun.fruitmix.file.data.model.FileTaskItem;

/**
 * Created by Administrator on 2017/7/28.
 */

public class FinishedTaskItem {

    private FileTaskItem fileTaskItem;
    private long fileTime;
    private String fileCreatorUUID;

    public FinishedTaskItem(FileTaskItem fileTaskItem) {
        this.fileTaskItem = fileTaskItem;
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
        return fileTaskItem.getFileName();
    }

    public long getFileSize() {
        return fileTaskItem.getFileSize();
    }

    public String getFileUUID() {
        return fileTaskItem.getFileUUID();
    }

    public FileTaskItem getFileTaskItem() {
        return fileTaskItem;
    }
}
