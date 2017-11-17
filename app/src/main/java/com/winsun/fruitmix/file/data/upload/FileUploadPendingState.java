package com.winsun.fruitmix.file.data.upload;

import com.winsun.fruitmix.file.data.download.TaskState;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

/**
 * Created by Administrator on 2017/11/15.
 */

public class FileUploadPendingState extends FileUploadState {

    private UploadFileUseCase uploadFileUseCase;

    public FileUploadPendingState(FileUploadItem fileUploadItem, UploadFileUseCase uploadFileUseCase) {
        super(fileUploadItem);
        this.uploadFileUseCase = uploadFileUseCase;
    }

    public UploadFileUseCase getUploadFileUseCase() {
        return uploadFileUseCase;
    }

    @Override
    public TaskState getDownloadState() {
        return TaskState.PENDING;
    }

    @Override
    public void startWork() {

    }
}
