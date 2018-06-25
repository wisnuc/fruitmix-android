package com.winsun.fruitmix.newdesign201804.logout

import android.content.Context
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource

class InjectLogoutUseCase {

    companion object {

        fun inject(context: Context):LogoutUseCase{
            return LogoutUseCase(InjectSystemSettingDataSource.provideSystemSettingDataSource(context),
                    InjectHttp.provideHttpRequestFactory(context))
        }

    }

}