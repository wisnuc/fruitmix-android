package com.winsun.fruitmix.newdesign201804.logout

import com.winsun.fruitmix.http.request.factory.HttpRequestFactory
import com.winsun.fruitmix.newdesign201804.exitApp.ExitAppUseCase
import com.winsun.fruitmix.system.setting.SystemSettingDataSource

class LogoutUseCase(val systemSettingDataSource: SystemSettingDataSource,val httpRequestFactory: HttpRequestFactory,
                    val exitAppUseCase: ExitAppUseCase) {

    fun logout(){

        systemSettingDataSource.currentLoginToken = ""
        systemSettingDataSource.currentLoginUserUUID = ""
        systemSettingDataSource.currentEquipmentIp = ""

        httpRequestFactory.reset()

        exitAppUseCase.exitApp()

    }


}