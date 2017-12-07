package com.winsun.fruitmix.login;

import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.FileTaskManager;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.file.data.upload.UploadFileUseCase;
import com.winsun.fruitmix.logout.LogoutUseCase;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.util.FileTool;

/**
 * Created by Administrator on 2017/12/4.
 */

public class LoginWithExistUserCallbackWrapper<T> implements BaseOperateDataCallback<T> {

    public static final String TAG = LoginNewUserCallbackWrapper.class.getSimpleName();

    private BaseOperateDataCallback<T> mCallback;

    private String temporaryUploadFolderParentFolderPath;

    private FileTool mFileTool;

    private UploadFileUseCase mUploadFileUseCase;

    private NetworkStateManager mNetworkStateManager;

    private FileTaskManager mFileTaskManager;

    private SystemSettingDataSource mSystemSettingDataSource;

    private StationFileRepository mStationFileRepository;

    LoginWithExistUserCallbackWrapper(String temporaryUploadFolderParentFolderPath, FileTool fileTool,
                                             UploadFileUseCase uploadFileUseCase, NetworkStateManager networkStateManager,
                                             FileTaskManager fileTaskManager, SystemSettingDataSource systemSettingDataSource,
                                             StationFileRepository stationFileRepository) {
        this.temporaryUploadFolderParentFolderPath = temporaryUploadFolderParentFolderPath;
        mFileTool = fileTool;
        mUploadFileUseCase = uploadFileUseCase;
        mNetworkStateManager = networkStateManager;
        mFileTaskManager = fileTaskManager;
        mSystemSettingDataSource = systemSettingDataSource;
        mStationFileRepository = stationFileRepository;

    }

    public void setCallback(BaseOperateDataCallback<T> callback) {
        mCallback = callback;
    }

    @Override
    public void onFail(OperationResult operationResult) {
        mCallback.onFail(operationResult);
    }

    @Override
    public void onSucceed(T data, OperationResult result) {

        String currentUserUUID = mSystemSettingDataSource.getCurrentLoginUserUUID();

        mStationFileRepository.fillAllFinishTaskItemIntoFileTaskManager(currentUserUUID);

        mFileTaskManager.initPendingUploadItem(mUploadFileUseCase, mNetworkStateManager, temporaryUploadFolderParentFolderPath,
                currentUserUUID, mFileTool);

        mCallback.onSucceed(data, result);

    }

    String getTemporaryUploadFolderParentFolderPath() {
        return temporaryUploadFolderParentFolderPath;
    }

    FileTool getFileTool() {
        return mFileTool;
    }

    UploadFileUseCase getUploadFileUseCase() {
        return mUploadFileUseCase;
    }

    NetworkStateManager getNetworkStateManager() {
        return mNetworkStateManager;
    }

    FileTaskManager getFileTaskManager() {
        return mFileTaskManager;
    }

    SystemSettingDataSource getSystemSettingDataSource() {
        return mSystemSettingDataSource;
    }

    public BaseOperateDataCallback<T> getCallback() {
        return mCallback;
    }
}
