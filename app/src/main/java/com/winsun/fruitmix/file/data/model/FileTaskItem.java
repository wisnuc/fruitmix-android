package com.winsun.fruitmix.file.data.model;

import android.util.Log;

import com.winsun.fruitmix.file.data.download.TaskState;

import java.util.concurrent.Future;

/**
 * Created by Administrator on 2017/11/15.
 */

public abstract class FileTaskItem {

    public static final String TAG = FileTaskItem.class.getSimpleName();

    private String fileUUID;

    private String fileName;
    private long fileSize;

    private long fileTime;

    private long fileCurrentTaskSize;

    private Future<Boolean> future;

    public FileTaskItem() {
    }

    public FileTaskItem(long fileSize) {
        this.fileSize = fileSize;
    }

    public FileTaskItem(String fileUUID, String fileName, long fileSize) {
        this.fileUUID = fileUUID;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public void setFileUUID(String fileUUID) {
        this.fileUUID = fileUUID;
    }

    public String getFileUUID() {
        return fileUUID;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileTime(long fileTime) {
        this.fileTime = fileTime;
    }

    public long getFileTime() {
        return fileTime;
    }

    public void setFileCurrentTaskSize(long fileCurrentTaskSize) {
        this.fileCurrentTaskSize = fileCurrentTaskSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getFileCurrentTaskSize() {
        return fileCurrentTaskSize;
    }

    public abstract TaskState getTaskState() ;

    public abstract String getUnionKey();

    public String getFileName() {
        return fileName;
    }

    public void setFuture(Future<Boolean> future) {
        this.future = future;
    }

    public void cancelTaskItem() {
        if (future != null)
            future.cancel(true);
    }

    public int getCurrentProgress(int max) {

        float currentProgress = getFileCurrentTaskSize() * max / getFileSize();

        return (int) currentProgress;
    }

}
