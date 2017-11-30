package com.winsun.fruitmix.logout;

import android.util.Log;

import com.winsun.fruitmix.file.data.model.FileTaskManager;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.util.FileTool;
import com.winsun.fruitmix.wechat.user.WeChatUser;
import com.winsun.fruitmix.wechat.user.WeChatUserDataSource;

import java.io.File;
import java.util.Collections;

/**
 * Created by Administrator on 2017/7/28.
 */

public class LogoutUseCase {

    public static final String TAG = LogoutUseCase.class.getSimpleName();

    private static LogoutUseCase ourInstance;

    private SystemSettingDataSource systemSettingDataSource;

    private LoggedInUserDataSource loggedInUserDataSource;

    private WeChatUserDataSource weChatUserDataSource;

    private UploadMediaUseCase uploadMediaUseCase;

    private HttpRequestFactory httpRequestFactory;

    private String temporaryUploadFolderParentFolderPath;

    private FileTool mFileTool;

    private StationFileRepository mStationFileRepository;

    private FileTaskManager mFileTaskManager;

    public static LogoutUseCase getInstance(SystemSettingDataSource systemSettingDataSource,
                                            LoggedInUserDataSource loggedInUserDataSource, UploadMediaUseCase uploadMediaUseCase,
                                            WeChatUserDataSource weChatUserDataSource, HttpRequestFactory httpRequestFactory, String temporaryUploadFolderParentFolderPath, FileTool fileTool,
                                            StationFileRepository stationFileRepository, FileTaskManager fileTaskManager) {
        if (ourInstance == null)
            ourInstance = new LogoutUseCase(systemSettingDataSource, loggedInUserDataSource,
                    uploadMediaUseCase, weChatUserDataSource, httpRequestFactory, temporaryUploadFolderParentFolderPath,
                    fileTool, stationFileRepository, fileTaskManager);
        return ourInstance;
    }

    public static void destroyInstance() {

        ourInstance = null;

    }

    private LogoutUseCase(SystemSettingDataSource systemSettingDataSource,
                          LoggedInUserDataSource loggedInUserDataSource, UploadMediaUseCase uploadMediaUseCase,
                          WeChatUserDataSource weChatUserDataSource, HttpRequestFactory httpRequestFactory,
                          String temporaryUploadFolderParentFolderPath, FileTool fileTool,
                          StationFileRepository stationFileRepository, FileTaskManager fileTaskManager) {

        this.systemSettingDataSource = systemSettingDataSource;
        this.loggedInUserDataSource = loggedInUserDataSource;
        this.uploadMediaUseCase = uploadMediaUseCase;
        this.weChatUserDataSource = weChatUserDataSource;
        this.httpRequestFactory = httpRequestFactory;
        this.temporaryUploadFolderParentFolderPath = temporaryUploadFolderParentFolderPath;
        mFileTool = fileTool;
        mStationFileRepository = stationFileRepository;
        mFileTaskManager = fileTaskManager;
    }

    public void changeLoginUser() {

        mFileTaskManager.cancelAllStartItem();

        mStationFileRepository.clearAllFileTaskItemInCache();

        Log.d(TAG, "change login user: cancel all start item and clearAllFileTaskItemInCache");

        uploadMediaUseCase.stopUploadMedia();

        uploadMediaUseCase.stopRetryUploadTemporary();

    }

    public void logout() {

        String currentLoginToken = systemSettingDataSource.getCurrentLoginToken();

        LoggedInUser loggedInUser = loggedInUserDataSource.getLoggedInUserByToken(currentLoginToken);

        if (loggedInUser != null)
            loggedInUserDataSource.deleteLoggedInUsers(Collections.singletonList(loggedInUser));
        else {

            String currentLoginStationID = systemSettingDataSource.getCurrentLoginStationID();

            WeChatUser weChatUser = weChatUserDataSource.getWeChatUser(currentLoginToken, currentLoginStationID);

            if (weChatUser != null)
                weChatUserDataSource.deleteWeChatUser(currentLoginToken);

        }

        systemSettingDataSource.setCurrentLoginToken("");

        systemSettingDataSource.setCurrentWAToken("");

        systemSettingDataSource.setCurrentLoginUserGUID("");

        systemSettingDataSource.setCurrentLoginStationID("");

        systemSettingDataSource.setCurrentEquipmentIp("");

        systemSettingDataSource.setCurrentLoginUserUUID("");

        systemSettingDataSource.setCurrentUploadUserUUID("");

        httpRequestFactory.reset();

        changeLoginUser();

        deleteTemporaryUploadFile();

    }

    private void deleteTemporaryUploadFile() {

        String folderPath = mFileTool.getTemporaryUploadFolderPath(temporaryUploadFolderParentFolderPath, systemSettingDataSource.getCurrentLoginUserUUID());

        boolean result = mFileTool.deleteDir(folderPath);

        Log.d(TAG, "logout,deleteTemporaryUploadFile,path:" + folderPath + "result: " + result);

    }


}
