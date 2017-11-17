package com.winsun.fruitmix.file.data.download;

import android.util.Log;

import com.winsun.fruitmix.file.data.model.FileTaskItem;

import java.util.concurrent.Future;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadItem extends FileTaskItem {

    public static final String TAG = FileDownloadItem.class.getSimpleName();

    private String parentFolderUUID;

    private String driveUUID;

    private FileDownloadState fileDownloadState;

    public FileDownloadItem(String fileName, long fileSize, String fileUUID) {
        super(fileUUID,fileName,fileSize);
    }

    public FileDownloadItem(String fileName, long fileSize, String fileUUID, String parentFolderUUID, String driveUUID) {
        super(fileUUID,fileName,fileSize);
        this.parentFolderUUID = parentFolderUUID;
        this.driveUUID = driveUUID;
    }

    public void setFileDownloadState(FileDownloadState fileDownloadState) {
        this.fileDownloadState = fileDownloadState;

        fileDownloadState.setFileUUID(getFileUUID());
        fileDownloadState.setFileName(getFileName());
        fileDownloadState.setFileSize(getFileSize());

        fileDownloadState.startWork();

        fileDownloadState.notifyDownloadStateChanged();
    }

    public String getParentFolderUUID() {
        return parentFolderUUID;
    }

    public String getDriveUUID() {
        return driveUUID;
    }

    @Override
    public TaskState getTaskState() {
        return fileDownloadState.getDownloadState();
    }

    @Override
    public String getUnionKey() {
        return getFileUUID();
    }

    public FileDownloadState getFileDownloadState() {
        return fileDownloadState;
    }


}
