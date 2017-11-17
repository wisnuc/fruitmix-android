package com.winsun.fruitmix.file.data.download;

import com.winsun.fruitmix.eventbus.TaskStateChangedEvent;
import com.winsun.fruitmix.file.data.model.FileTaskState;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/11/7.
 */

public abstract class FileDownloadState extends FileTaskState{

    private String fileName;
    private String fileUUID;

    public FileDownloadState(FileDownloadItem fileDownloadItem) {
        super(fileDownloadItem);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileUUID(String fileUUID) {
        this.fileUUID = fileUUID;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUUID() {
        return fileUUID;
    }

    public FileDownloadItem getFileDownloadItem() {
        return (FileDownloadItem) getFileTaskItem();
    }

    public String getParentFolderUUID() {
        return getFileDownloadItem().getParentFolderUUID();
    }

    public String getDriveUUID(){
        return getFileDownloadItem().getDriveUUID();
    }
}
