package com.winsun.fruitmix.newdesign201804.equipment.add.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.equipment.search.data.Equipment
import com.winsun.fruitmix.newdesign201804.equipment.add.AvailableEquipmentInfo
import com.winsun.fruitmix.newdesign201804.equipment.add.ReinitializationEquipmentInfo
import com.winsun.fruitmix.newdesign201804.equipment.add.UnBoundEquipmentInfo

interface NewEquipmentInfoDataSource {

    fun getAvailableEquipmentInfo(equipment:Equipment,baseLoadDataCallback: BaseLoadDataCallback<AvailableEquipmentInfo>)

    fun getUnboundEquipmentInfo(equipment: Equipment,baseLoadDataCallback: BaseLoadDataCallback<UnBoundEquipmentInfo>)

    fun getReinitializationEquipmentInfo(equipment: Equipment,baseLoadDataCallback: BaseLoadDataCallback<ReinitializationEquipmentInfo>)

}