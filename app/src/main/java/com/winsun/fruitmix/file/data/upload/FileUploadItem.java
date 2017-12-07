package com.winsun.fruitmix.file.data.upload;

import android.util.Log;

import com.winsun.fruitmix.file.data.download.TaskState;
import com.winsun.fruitmix.file.data.model.FileTaskItem;
import com.winsun.fruitmix.util.FileTool;

import java.io.File;

/**
 * Created by Administrator on 2017/11/15.
 */

public class FileUploadItem extends FileTaskItem {

    private String filePath;

    private FileUploadState fileUploadState;

    private String temporaryUploadFilePath;

    public FileUploadItem() {
    }

    public FileUploadItem(String fileUUID, String fileName, long fileSize, String filePath) {
        super(fileUUID, fileName, fileSize);
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

    public void setTemporaryUploadFilePath(String temporaryUploadFilePath) {
        this.temporaryUploadFilePath = temporaryUploadFilePath;
    }

    public String getTemporaryUploadFilePath() {
        return temporaryUploadFilePath;
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
