package com.winsun.fruitmix.file.data.upload;

import android.util.Log;

import com.winsun.fruitmix.file.data.download.TaskState;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

/**
 * Created by Administrator on 2017/11/15.
 */

public class FileUploadPendingState extends FileUploadState {

    public static final String TAG = FileUploadPendingState.class.getSimpleName();

    private UploadFileUseCase uploadFileUseCase;

    private NetworkStateManager networkStateManager;

    public FileUploadPendingState(FileUploadItem fileUploadItem, UploadFileUseCase uploadFileUseCase,NetworkStateManager networkStateManager) {
        super(fileUploadItem);
        this.uploadFileUseCase = uploadFileUseCase;
        this.networkStateManager = networkStateManager;
    }

    public UploadFileUseCase getUploadFileUseCase() {
        return uploadFileUseCase;
    }

    @Override
    public TaskState getDownloadState() {
        return TaskState.PENDING;
    }

    public NetworkStateManager getNetworkStateManager() {
        return networkStateManager;
    }

    @Override
    public void startWork() {
        Log.d(TAG, "startWork: it is pending now");
    }

}
