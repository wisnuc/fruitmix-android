package com.winsun.fruitmix.file.data.model;

import com.winsun.fruitmix.eventbus.TaskStateChangedEvent;
import com.winsun.fruitmix.file.data.download.TaskState;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/11/15.
 */

public abstract class FileTaskState {

    private FileTaskItem fileTaskItem;

    public FileTaskState(FileTaskItem fileTaskItem) {
        this.fileTaskItem = fileTaskItem;
    }

    public void setFileSize(long fileSize) {
        fileTaskItem.setFileSize(fileSize);
    }

    public void setFileCurrentDownloadSize(long fileCurrentDownloadSize) {
        fileTaskItem.setFileCurrentTaskSize(fileCurrentDownloadSize);
    }

    protected FileTaskItem getFileTaskItem() {
        return fileTaskItem;
    }

    public abstract TaskState getDownloadState();

    public abstract void startWork();

    public void notifyDownloadStateChanged() {

        EventBus.getDefault().postSticky(new TaskStateChangedEvent(getDownloadState()));

    }

}
