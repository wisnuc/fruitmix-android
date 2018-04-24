package com.winsun.fruitmix.newdesign201804.equipment.list.data

import android.util.Log
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.equipment.list.EquipmentItem
import com.winsun.fruitmix.newdesign201804.equipment.list.EquipmentType

private const val TAG = "FakeEquipmentItemData"

object FakeEquipmentItemDataSource : EquipmentItemDataSource {

    private val equipmentItems: MutableList<EquipmentItem> = mutableListOf()

    private var cacheDirty:Boolean

    init {

        cacheDirty = true

        equipmentItems.add(EquipmentItem(EquipmentType.CLOUD_CONNECTED, "test1"))
        equipmentItems.add(EquipmentItem(EquipmentType.CLOUD_UNCONNECTED, "test2"))
        equipmentItems.add(EquipmentItem(EquipmentType.DISK_ABNORMAL, "test3"))
        equipmentItems.add(EquipmentItem(EquipmentType.POWER_OFF, "test4"))
        equipmentItems.add(EquipmentItem(EquipmentType.UNDER_REVIEW, "test5"))
        equipmentItems.add(EquipmentItem(EquipmentType.OFFLINE, "test6"))

    }

    override fun getEquipmentItems(baseLoadDataCallback: BaseLoadDataCallback<EquipmentItem>) {

        cacheDirty = false

        Log.d(TAG, "cacheDirty: ${isCacheDirty()}")

        baseLoadDataCallback.onSucceed(equipmentItems, OperationSuccess())

    }

    override fun addEquipmentItems(equipmentItem: EquipmentItem, baseOperateCallback: BaseOperateCallback) {

        equipmentItems.add(equipmentItem)

        cacheDirty = true

        Log.d(TAG, "cacheDirty: ${isCacheDirty()}")

        baseOperateCallback.onSucceed()

    }

    fun resetCacheDirty(){
        cacheDirty = true
    }


    override fun isCacheDirty():Boolean{
        return cacheDirty
    }

}