package com.winsun.fruitmix.file.data.upload;

import com.winsun.fruitmix.file.data.download.TaskState;
import com.winsun.fruitmix.file.data.model.FileTaskItem;

/**
 * Created by Administrator on 2017/11/15.
 */

public class FileUploadItem extends FileTaskItem {

    private String filePath;

    private String fileSourceFromAppName;

    private FileUploadState fileUploadState;

    public FileUploadItem(String fileUUID,String fileName, long fileSize, String filePath) {
        super(fileUUID,fileName, fileSize);
        this.filePath = filePath;

        fileSourceFromAppName = "";
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFileSourceFromAppName(String fileSourceFromAppName) {
        this.fileSourceFromAppName = fileSourceFromAppName;
    }

    public String getFileSourceFromAppName() {
        return fileSourceFromAppName;
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
