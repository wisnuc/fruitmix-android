package com.winsun.fruitmix.file.data.upload;

import com.winsun.fruitmix.file.data.download.FileTaskManager;
import com.winsun.fruitmix.file.data.download.TaskState;

/**
 * Created by Administrator on 2017/11/15.
 */

public class FileUploadErrorState extends FileUploadState {

    public FileUploadErrorState(FileUploadItem fileUploadItem) {
        super(fileUploadItem);
    }

    @Override
    public TaskState getDownloadState() {
        return TaskState.ERROR;
    }

    @Override
    public void startWork() {
        FileTaskManager.getInstance().startPendingTaskItem();
    }

}
