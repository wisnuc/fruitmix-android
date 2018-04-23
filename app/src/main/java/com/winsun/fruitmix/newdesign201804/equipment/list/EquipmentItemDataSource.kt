package com.winsun.fruitmix.newdesign201804.equipment.list

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback

interface EquipmentItemDataSource {

    fun getEquipmentItems(baseLoadDataCallback: BaseLoadDataCallback<EquipmentItem>)
    fun addEquipmentItems(equipmentItem: EquipmentItem, baseOperateCallback: BaseOperateCallback)

    fun isCacheDirty():Boolean
}