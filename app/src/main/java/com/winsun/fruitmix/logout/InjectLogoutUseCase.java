package com.winsun.fruitmix.logout;

import android.content.Context;

import com.winsun.fruitmix.file.data.model.FileTaskManager;
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.mqtt.InjectMqttUseCase;
import com.winsun.fruitmix.mqtt.MqttUseCase;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.upload.media.InjectUploadMediaUseCase;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.FileTool;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.wechat.user.InjectWeChatUserDataSource;

/**
 * Created by Administrator on 2017/7/28.
 */

public class InjectLogoutUseCase {

    public static LogoutUseCase provideLogoutUseCase(Context context) {

        return LogoutUseCase.getInstance(InjectSystemSettingDataSource.provideSystemSettingDataSource(context), InjectLoggedInUser.provideLoggedInUserRepository(context),
                InjectUploadMediaUseCase.provideUploadMediaUseCase(context), InjectWeChatUserDataSource.provideWeChatUserDataSource(context),
                InjectHttp.provideHttpRequestFactory(context), FileUtil.getTemporaryDataFolderParentFolderPath(context),
                FileTool.getInstance(), InjectStationFileRepository.provideStationFileRepository(context),
                InjectUser.provideRepository(context), FileTaskManager.getInstance(), InjectMqttUseCase.provideInstance(context));

    }

}
