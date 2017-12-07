package com.winsun.fruitmix.login;

import com.winsun.fruitmix.file.data.model.FileTaskManager;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.file.data.upload.UploadFileUseCase;
import com.winsun.fruitmix.logout.LogoutUseCase;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.util.FileTool;

/**
 * Created by Administrator on 2017/11/24.
 */

public class LoginNewUserCallbackWrapper<T> extends LoginWithExistUserCallbackWrapper<T> {

    public static final String TAG = LoginNewUserCallbackWrapper.class.getSimpleName();

    private LogoutUseCase mLogoutUseCase;

    public LoginNewUserCallbackWrapper(String temporaryUploadFolderParentFolderPath, FileTool fileTool,
                                       UploadFileUseCase uploadFileUseCase, NetworkStateManager networkStateManager,
                                       FileTaskManager fileTaskManager, SystemSettingDataSource systemSettingDataSource,
                                       StationFileRepository stationFileRepository, LogoutUseCase logoutUseCase) {

        super(temporaryUploadFolderParentFolderPath, fileTool, uploadFileUseCase, networkStateManager, fileTaskManager,
                systemSettingDataSource, stationFileRepository);

        mLogoutUseCase = logoutUseCase;

    }

    @Override
    public void onFail(OperationResult operationResult) {
        getCallback().onFail(operationResult);
    }

    @Override
    public void onSucceed(T data, OperationResult result) {

        mLogoutUseCase.stopUploadTask();

        super.onSucceed(data, result);

    }
}
