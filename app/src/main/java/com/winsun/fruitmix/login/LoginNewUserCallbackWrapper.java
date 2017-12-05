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

public class LoginNewUserCallbackWrapper<T> extends LoginWithExistUserCallbackWrapper<T> {

    public static final String TAG = LoginNewUserCallbackWrapper.class.getSimpleName();

    private BaseOperateDataCallback<T> mCallback;

    private LogoutUseCase mLogoutUseCase;

    public LoginNewUserCallbackWrapper(String temporaryUploadFolderParentFolderPath, FileTool fileTool,
                                       UploadFileUseCase uploadFileUseCase, NetworkStateManager networkStateManager,
                                       FileTaskManager fileTaskManager, SystemSettingDataSource systemSettingDataSource,
                                       LogoutUseCase logoutUseCase) {
        super(temporaryUploadFolderParentFolderPath, fileTool, uploadFileUseCase, networkStateManager, fileTaskManager, systemSettingDataSource);
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

        getFileTaskManager().initPendingUploadItem(getUploadFileUseCase(), getNetworkStateManager(), getTemporaryUploadFolderParentFolderPath(),
                getSystemSettingDataSource().getCurrentLoginUserUUID(),getFileTool());

        mCallback.onSucceed(data, result);

    }
}
