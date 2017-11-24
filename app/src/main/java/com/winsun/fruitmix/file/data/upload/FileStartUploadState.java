package com.winsun.fruitmix.file.data.upload;

import com.winsun.fruitmix.executor.UploadFileTask;
import com.winsun.fruitmix.file.data.download.FileDownloadErrorState;
import com.winsun.fruitmix.file.data.download.TaskState;
import com.winsun.fruitmix.network.NetworkState;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import java.util.concurrent.Future;

/**
 * Created by Administrator on 2017/11/15.
 */

public class FileStartUploadState extends FileUploadState {

    private ThreadManager threadManager;

    private UploadFileUseCase uploadFileUseCase;

    private NetworkStateManager networkStateManager;

    public FileStartUploadState(FileUploadItem fileUploadItem, ThreadManager threadManager,UploadFileUseCase uploadFileUseCase,
                                NetworkStateManager networkStateManager) {
        super(fileUploadItem);
        this.uploadFileUseCase = uploadFileUseCase;
        this.threadManager = threadManager;
        this.networkStateManager = networkStateManager;
    }

    @Override
    public TaskState getDownloadState() {
        return TaskState.START_DOWNLOAD_OR_UPLOAD;
    }

    @Override
    public void startWork() {

        UploadFileTask uploadFileTask = new UploadFileTask(this,uploadFileUseCase);

        Future<Boolean> future = threadManager.runOnDownloadFileThread(uploadFileTask);

        getFileUploadItem().setFuture(future);

    }
}
