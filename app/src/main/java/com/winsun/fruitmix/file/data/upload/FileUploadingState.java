package com.winsun.fruitmix.file.data.upload;

import com.winsun.fruitmix.file.data.download.TaskState;

/**
 * Created by Administrator on 2017/11/15.
 */

public class FileUploadingState extends FileUploadState {

    public FileUploadingState(FileUploadItem fileUploadItem) {
        super(fileUploadItem);
    }

    @Override
    public TaskState getDownloadState() {
        return TaskState.DOWNLOADING_OR_UPLOADING;
    }

    @Override
    public void startWork() {

    }
}
