package com.winsun.fruitmix.logout;

import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.wechat.user.WeChatUser;
import com.winsun.fruitmix.wechat.user.WeChatUserDataSource;

import java.util.Collections;

/**
 * Created by Administrator on 2017/7/28.
 */

public class LogoutUseCase {

    private static LogoutUseCase ourInstance;

    private SystemSettingDataSource systemSettingDataSource;

    private LoggedInUserDataSource loggedInUserDataSource;

    private WeChatUserDataSource weChatUserDataSource;

    private UploadMediaUseCase uploadMediaUseCase;

    public static LogoutUseCase getInstance(SystemSettingDataSource systemSettingDataSource,
                                            LoggedInUserDataSource loggedInUserDataSource, UploadMediaUseCase uploadMediaUseCase,
                                            WeChatUserDataSource weChatUserDataSource) {
        if (ourInstance == null)
            ourInstance = new LogoutUseCase(systemSettingDataSource, loggedInUserDataSource, uploadMediaUseCase, weChatUserDataSource);
        return ourInstance;
    }

    public static void destroyInstance(){

        ourInstance = null;

    }

    private LogoutUseCase(SystemSettingDataSource systemSettingDataSource,
                          LoggedInUserDataSource loggedInUserDataSource, UploadMediaUseCase uploadMediaUseCase,
                          WeChatUserDataSource weChatUserDataSource) {

        this.systemSettingDataSource = systemSettingDataSource;
        this.loggedInUserDataSource = loggedInUserDataSource;
        this.uploadMediaUseCase = uploadMediaUseCase;

        this.weChatUserDataSource = weChatUserDataSource;

    }

    public void changeLoginUser() {

        uploadMediaUseCase.stopUploadMedia();

        uploadMediaUseCase.stopRetryUpload();

    }

    public void logout() {

        String currentLoginToken = systemSettingDataSource.getCurrentLoginToken();

        LoggedInUser loggedInUser = loggedInUserDataSource.getLoggedInUserByToken(currentLoginToken);

        if (loggedInUser != null)
            loggedInUserDataSource.deleteLoggedInUsers(Collections.singletonList(loggedInUser));
        else {

            String currentLoginUserGUID = systemSettingDataSource.getCurrentLoginUserGUID();

            WeChatUser weChatUser = weChatUserDataSource.getWeChatUser(currentLoginToken,currentLoginUserGUID);

            if (weChatUser != null)
                weChatUserDataSource.deleteWeChatUser(currentLoginToken);

        }

        systemSettingDataSource.setCurrentLoginToken("");

        systemSettingDataSource.setCurrentLoginUserGUID("");

        changeLoginUser();

    }


}
