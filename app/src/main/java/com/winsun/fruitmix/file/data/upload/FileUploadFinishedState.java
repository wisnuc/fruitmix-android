package com.winsun.fruitmix.file.data.upload;

import com.winsun.fruitmix.file.data.download.FileTaskManager;
import com.winsun.fruitmix.file.data.download.TaskState;
import com.winsun.fruitmix.file.data.model.FileTaskState;

/**
 * Created by Administrator on 2017/11/15.
 */

public class FileUploadFinishedState extends FileUploadState {

    public FileUploadFinishedState(FileUploadItem fileUploadItem) {
        super(fileUploadItem);
    }

    @Override
    public TaskState getDownloadState() {
        return TaskState.FINISHED;
    }

    @Override
    public void startWork() {

        FileTaskManager.getInstance().startPendingTaskItem();

    }
}
