package com.winsun.fruitmix.newdesign201804.login

import android.view.View
import com.winsun.fruitmix.newdesign201804.equipment.list.data.EquipmentItemDataSource
import com.winsun.fruitmix.newdesign201804.equipment.model.CloudUnConnectedEquipmentItem
import kotlinx.android.synthetic.main.activity_lan_login.view.*

class LanLoginPresenter(private val itemUUID:String,private val equipmentItemDataSource: EquipmentItemDataSource) {

    fun initView(rootView: View){

        val cloudUnConnectedEquipmentItem = equipmentItemDataSource.getEquipmentItemInCache(itemUUID) as CloudUnConnectedEquipmentItem

        rootView.equipmentNameTv.text = cloudUnConnectedEquipmentItem.name
        rootView.equipmentTypeTv.text = cloudUnConnectedEquipmentItem.type

        rootView.userNameTv.text = cloudUnConnectedEquipmentItem.loginUser.userName

    }


}