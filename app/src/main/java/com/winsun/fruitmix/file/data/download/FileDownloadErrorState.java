package com.winsun.fruitmix.file.data.download;

import com.winsun.fruitmix.file.data.model.FileTaskManager;

/**
 * Created by Administrator on 2016/11/9.
 */

public class FileDownloadErrorState extends FileDownloadState {

    public FileDownloadErrorState(FileDownloadItem fileDownloadItem) {
        super(fileDownloadItem);
    }

    @Override
    public TaskState getDownloadState() {
        return TaskState.ERROR;
    }

    @Override
    public void startWork() {

        FileTaskManager.getInstance().startPendingDownloadTaskItem();

    }

}
