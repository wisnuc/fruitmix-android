package com.winsun.fruitmix.newdesign201804.equipment.add.data

import android.content.Context
import com.winsun.fruitmix.R
import com.winsun.fruitmix.user.User

open class BaseNewEquipmentInfo(val equipmentName: String, val equipmentIP: String)

class UnBoundEquipmentInfo(equipmentName: String, equipmentIP: String,
                           val unboundEquipmentDiskInfos: List<UnboundEquipmentDiskInfo>) : BaseNewEquipmentInfo(equipmentName, equipmentIP) {

    var selectBoundEquipmentDiskInfo: UnboundEquipmentDiskInfo? = null

}

class AvailableEquipmentInfo(equipmentName: String, equipmentIP: String,
                             val availableEquipmentDiskInfo: AvailableEquipmentDiskInfo) : BaseNewEquipmentInfo(equipmentName, equipmentIP)

class ReinitializationEquipmentInfo(equipmentName: String, equipmentIP: String) : BaseNewEquipmentInfo(equipmentName, equipmentIP)

enum class DiskMode {
    SINGLE, RAID1
}

fun convertDiskMode(diskMode: DiskMode, context:Context):String{
   return when (diskMode) {
        DiskMode.SINGLE -> context.getString(R.string.single)
        DiskMode.RAID1 -> context.getString(R.string.raid1)
    }
}

data class UnboundEquipmentDiskInfo(val originalEquipmentName: String, val diskMode: DiskMode, val availableDiskSize: Double, val totalDiskSize: Double)

data class AvailableEquipmentDiskInfo(val admin: User, val availableDiskSize: Double, val totalDiskSize: Double)
