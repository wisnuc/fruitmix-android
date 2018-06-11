package com.winsun.fruitmix.newdesign201804.login

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory
import com.winsun.fruitmix.model.operationResult.OperationFail
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.stations.StationsDataSource
import com.winsun.fruitmix.system.setting.SystemSettingDataSource
import com.winsun.fruitmix.token.data.TokenDataSource
import com.winsun.fruitmix.token.param.StationTokenParam
import com.winsun.fruitmix.user.User
import com.winsun.fruitmix.user.datasource.UserDataRepository

class LoginUseCase(val tokenDataSource: TokenDataSource, val systemSettingDataSource: SystemSettingDataSource,
                   val httpRequestFactory: HttpRequestFactory, val userDataRepository: UserDataRepository,
                   val stationsDataSource: StationsDataSource) {

    fun loginWithNoParam(baseOperateCallback: BaseOperateCallback) {

        val token = systemSettingDataSource.currentLoginToken

        return if (token.isNotEmpty()) {

            val loginUserUUID = systemSettingDataSource.currentLoginUserUUID
            val gateway = systemSettingDataSource.currentEquipmentIp

            stationsDataSource.checkStationIP(gateway, object : BaseOperateDataCallback<Boolean> {
                override fun onFail(operationResult: OperationResult?) {
                    baseOperateCallback.onFail(OperationFail("ip is unreachable"))
                }

                override fun onSucceed(data: Boolean?, result: OperationResult?) {
                    initSystem(token, gateway)

                    userDataRepository.clearAllUsersInCache()

                    getUser(loginUserUUID, baseOperateCallback)

                }
            })


        } else
            baseOperateCallback.onFail(OperationFail("no token"))

    }

    fun lanLogin(stationTokenParam: StationTokenParam, baseOperateCallback: BaseOperateCallback) {

        tokenDataSource.getStationToken(stationTokenParam, object : BaseLoadDataCallback<String> {

            override fun onSucceed(data: MutableList<String>?, operationResult: OperationResult?) {

                val token = data!![0]

                initSystem(token, stationTokenParam.gateway)

                systemSettingDataSource.currentLoginUserUUID = stationTokenParam.userUUID
                systemSettingDataSource.currentEquipmentIp = stationTokenParam.gateway
                systemSettingDataSource.currentLoginToken = token

                getUser(stationTokenParam.userUUID, baseOperateCallback)

            }

            override fun onFail(operationResult: OperationResult?) {

                baseOperateCallback.onFail(operationResult)

            }
        })

    }

    private fun initSystem(token: String?, gateway: String?) {
        httpRequestFactory.setCurrentData(token, gateway)
    }

    private fun getUser(currentUserUUID: String, baseOperateCallback: BaseOperateCallback) {

        userDataRepository.getUsers(currentUserUUID, object : BaseLoadDataCallbackImpl<User>() {

            override fun onSucceed(users: List<User>, operationResult: OperationResult) {
                super.onSucceed(users, operationResult)

                for (user in users) {

                    if (user.uuid == currentUserUUID) {

                        userDataRepository.getCurrentUserHome(object : BaseLoadDataCallback<String> {
                            override fun onSucceed(data: List<String>, operationResult: OperationResult) {

                                user.home = data[0]

                                userDataRepository.insertUsers(users)

                                baseOperateCallback.onSucceed()

                            }

                            override fun onFail(operationResult: OperationResult) {
                                baseOperateCallback.onFail(operationResult)
                            }
                        })

                    }

                }

            }

            override fun onFail(operationResult: OperationResult) {
                super.onFail(operationResult)

                baseOperateCallback.onFail(operationResult)
            }
        })

    }


}