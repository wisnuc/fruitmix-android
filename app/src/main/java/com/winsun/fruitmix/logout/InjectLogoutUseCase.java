package com.winsun.fruitmix.logout;

import android.content.Context;

import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.upload.media.InjectUploadMediaUseCase;
import com.winsun.fruitmix.wechat.user.InjectWeChatUserDataSource;

/**
 * Created by Administrator on 2017/7/28.
 */

public class InjectLogoutUseCase {

    public static LogoutUseCase provideLogoutUseCase(Context context) {

        return LogoutUseCase.getInstance(InjectSystemSettingDataSource.provideSystemSettingDataSource(context), InjectLoggedInUser.provideLoggedInUserRepository(context),
                InjectUploadMediaUseCase.provideUploadMediaUseCase(context), InjectWeChatUserDataSource.provideWeChatUserDataSource(context));

    }

}
