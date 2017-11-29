package com.winsun.fruitmix.login;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.FileTaskManager;
import com.winsun.fruitmix.file.data.upload.UploadFileUseCase;
import com.winsun.fruitmix.logout.LogoutUseCase;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.util.FileTool;

import java.io.File;

/**
 * Created by Administrator on 2017/11/24.
 */

public class LoginNewUserCallbackWrapper<T> implements BaseOperateDataCallback<T> {

    public static final String TAG = LoginNewUserCallbackWrapper.class.getSimpleName();

    private BaseOperateDataCallback<T> mCallback;

    private String temporaryUploadFolderParentFolderPath;

    private FileTool mFileTool;

    private UploadFileUseCase mUploadFileUseCase;

    private NetworkStateManager mNetworkStateManager;

    private FileTaskManager mFileTaskManager;

    private SystemSettingDataSource mSystemSettingDataSource;

    private LogoutUseCase mLogoutUseCase;

    public LoginNewUserCallbackWrapper(String temporaryUploadFolderParentFolderPath, FileTool fileTool,
                                       UploadFileUseCase uploadFileUseCase, NetworkStateManager networkStateManager,
                                       FileTaskManager fileTaskManager, SystemSettingDataSource systemSettingDataSource,
                                       LogoutUseCase logoutUseCase) {
        this.temporaryUploadFolderParentFolderPath = temporaryUploadFolderParentFolderPath;
        mFileTool = fileTool;
        mUploadFileUseCase = uploadFileUseCase;
        mNetworkStateManager = networkStateManager;
        mFileTaskManager = fileTaskManager;
        mSystemSettingDataSource = systemSettingDataSource;
        mLogoutUseCase = logoutUseCase;
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

        mLogoutUseCase.changeLoginUser();

        mFileTaskManager.initPendingUploadItem(mUploadFileUseCase, mNetworkStateManager, temporaryUploadFolderParentFolderPath,
                mSystemSettingDataSource.getCurrentLoginUserUUID(), mFileTool);

        mCallback.onSucceed(data, result);

    }
}
