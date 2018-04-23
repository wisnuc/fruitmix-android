package com.winsun.fruitmix.newdesign201804.equipment.list

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.model.operationResult.OperationSuccess

class FakeEquipmentItemDataSource : EquipmentItemDataSource {

    private val equipmentItems: MutableList<EquipmentItem> = mutableListOf()

    private var cacheDirty = true

    init {

        equipmentItems.add(EquipmentItem(EquipmentType.CLOUD_CONNECTED, "test1"))
        equipmentItems.add(EquipmentItem(EquipmentType.CLOUD_UNCONNECTED, "test2"))
        equipmentItems.add(EquipmentItem(EquipmentType.DISK_ABNORMAL, "test3"))
        equipmentItems.add(EquipmentItem(EquipmentType.POWER_OFF, "test4"))
        equipmentItems.add(EquipmentItem(EquipmentType.UNDER_REVIEW, "test5"))
        equipmentItems.add(EquipmentItem(EquipmentType.OFFLINE, "test6"))

    }


    override fun getEquipmentItems(baseLoadDataCallback: BaseLoadDataCallback<EquipmentItem>) {

        baseLoadDataCallback.onSucceed(equipmentItems, OperationSuccess())

        cacheDirty = false

    }

    override fun addEquipmentItems(equipmentItem: EquipmentItem, baseOperateCallback: BaseOperateCallback) {

        equipmentItems.add(equipmentItem)

        cacheDirty = true

    }


    override fun isCacheDirty() = cacheDirty

}