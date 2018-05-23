package com.winsun.fruitmix.newdesign201804.login

import android.content.Context
import android.view.View
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.equipment.search.data.Equipment
import com.winsun.fruitmix.equipment.search.data.EquipmentDataSource
import com.winsun.fruitmix.equipment.search.data.InjectEquipment
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.equipment.list.data.EquipmentItemDataSource
import com.winsun.fruitmix.newdesign201804.equipment.model.CloudUnConnectedEquipmentItem
import com.winsun.fruitmix.newdesign201804.mainpage.MainPageActivity
import com.winsun.fruitmix.token.param.StationTokenParam
import com.winsun.fruitmix.user.User
import kotlinx.android.synthetic.main.activity_lan_login.view.*

class LanLoginPresenter(private val itemUUID: String, private val equipmentItemDataSource: EquipmentItemDataSource,
                        val equipmentDataSource: EquipmentDataSource) {

    private lateinit var context: Context

    private lateinit var currentUser: User
    private lateinit var cloudUnConnectedEquipmentItem: CloudUnConnectedEquipmentItem

    fun initView(rootView: View) {

        cloudUnConnectedEquipmentItem = equipmentItemDataSource.getEquipmentItemInCache(itemUUID) as CloudUnConnectedEquipmentItem

        context = rootView.context

        rootView.equipmentNameTv.text = cloudUnConnectedEquipmentItem.name
        rootView.equipmentTypeTv.text = cloudUnConnectedEquipmentItem.type

        rootView.userNameTv.text = cloudUnConnectedEquipmentItem.loginUser.userName

        getUserInfo(cloudUnConnectedEquipmentItem.ip, rootView)

    }

    private fun getUserInfo(equipmentIP: String, rootView: View) {

        val equipment = Equipment()
        equipment.hosts = listOf(equipmentIP)

        equipmentDataSource.getUsersInEquipment(equipment, object : BaseLoadDataCallback<User> {
            override fun onFail(operationResult: OperationResult?) {

            }

            override fun onSucceed(data: MutableList<User>?, operationResult: OperationResult?) {

                currentUser = data!![0]

                rootView.userNameTv.text = currentUser.userName

            }
        })

    }

    fun lanLogin(password: String,
                 baseOperateCallback: BaseOperateCallback) {

        val loginUseCase = InjectLoginCase.provideInstance(context)

        val stationTokenParam = StationTokenParam(cloudUnConnectedEquipmentItem.ip, currentUser.uuid, password,
                cloudUnConnectedEquipmentItem.name)

        loginUseCase.lanLogin(stationTokenParam, object : BaseOperateCallback {
            override fun onFail(operationResult: OperationResult?) {

                baseOperateCallback.onFail(operationResult)
            }

            override fun onSucceed() {

                MainPageActivity.start(context, stationTokenParam.gateway, stationTokenParam.equipmentName)

                baseOperateCallback.onSucceed()

            }
        })

    }


}