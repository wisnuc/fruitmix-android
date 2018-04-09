package com.winsun.fruitmix.file.data.download;

import com.winsun.fruitmix.file.data.model.FileTaskManager;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadFinishedState extends FileDownloadState {

    public static final String TAG = FileDownloadFinishedState.class.getSimpleName();

    public FileDownloadFinishedState(FileDownloadItem fileDownloadItem) {
        super(fileDownloadItem);
    }

    @Override
    public TaskState getDownloadState() {
        return TaskState.FINISHED;
    }

    @Override
    public void startWork() {

        FileTaskManager.getInstance().startPendingDownloadTaskItem();

    }

}
