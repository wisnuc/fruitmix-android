package com.winsun.fruitmix.newdesign201804.equipment.list.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.newdesign201804.equipment.list.EquipmentItem

interface EquipmentItemDataSource {

    fun getEquipmentItems(baseLoadDataCallback: BaseLoadDataCallback<EquipmentItem>)
    fun addEquipmentItems(equipmentItem: EquipmentItem, baseOperateCallback: BaseOperateCallback)

    fun isCacheDirty():Boolean
}