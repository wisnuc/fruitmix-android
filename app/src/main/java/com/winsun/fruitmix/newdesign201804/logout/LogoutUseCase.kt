package com.winsun.fruitmix.newdesign201804.logout

import com.winsun.fruitmix.http.request.factory.HttpRequestFactory
import com.winsun.fruitmix.system.setting.SystemSettingDataSource

class LogoutUseCase(val systemSettingDataSource: SystemSettingDataSource,val httpRequestFactory: HttpRequestFactory) {

    fun logout(){

        systemSettingDataSource.currentLoginToken = ""
        systemSettingDataSource.currentLoginUserUUID = ""
        systemSettingDataSource.currentEquipmentIp = ""

        httpRequestFactory.reset()

    }


}