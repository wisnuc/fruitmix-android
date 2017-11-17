package com.winsun.fruitmix.file.data.upload;

import com.winsun.fruitmix.file.data.download.TaskState;
import com.winsun.fruitmix.file.data.model.FileTaskItem;

/**
 * Created by Administrator on 2017/11/15.
 */

public class FileUploadItem extends FileTaskItem {

    private String filePath;

    private FileUploadState fileUploadState;

    public FileUploadItem() {
    }

    public FileUploadItem(String fileUUID, String fileName, long fileSize, String filePath) {
        super(fileUUID,fileName, fileSize);
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileUploadState(FileUploadState fileUploadState) {
        this.fileUploadState = fileUploadState;

        fileUploadState.setFilePath(getFilePath());

        fileUploadState.startWork();

        fileUploadState.notifyDownloadStateChanged();

    }

    public FileUploadState getFileUploadState() {
        return fileUploadState;
    }

    @Override
    public String getUnionKey() {
        return getFileUUID();
    }

    @Override
    public TaskState getTaskState() {
        return fileUploadState.getDownloadState();
    }
}
