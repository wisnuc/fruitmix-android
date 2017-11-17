package com.winsun.fruitmix.file.data.upload;

import com.winsun.fruitmix.file.data.download.TaskState;
import com.winsun.fruitmix.file.data.model.FileTaskItem;
import com.winsun.fruitmix.file.data.model.FileTaskState;

/**
 * Created by Administrator on 2017/11/15.
 */

public abstract class FileUploadState extends FileTaskState {

    private String filePath;

    public FileUploadState(FileUploadItem fileUploadItem) {
        super(fileUploadItem);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public FileUploadItem getFileUploadItem() {
        return (FileUploadItem) getFileTaskItem();
    }

}
