package com.winsun.fruitmix.newdesign201804.equipment.list.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.newdesign201804.equipment.model.BaseEquipmentItem

interface EquipmentItemDataSource {

    fun getEquipmentItems(baseLoadDataCallback: BaseLoadDataCallback<BaseEquipmentItem>)
    fun addEquipmentItems(baseEquipmentItem: BaseEquipmentItem, baseOperateCallback: BaseOperateCallback)

    fun getEquipmentItemInCache(itemUUID:String):BaseEquipmentItem?

    fun isCacheDirty():Boolean
}