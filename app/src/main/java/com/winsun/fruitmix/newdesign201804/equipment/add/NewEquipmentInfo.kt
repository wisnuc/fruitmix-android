package com.winsun.fruitmix.newdesign201804.equipment.add

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

data class UnboundEquipmentDiskInfo(val originalEquipmentName: String, val diskMode: DiskMode, val availableDiskSize: Long, val totalDiskSize: Long)

data class AvailableEquipmentDiskInfo(val admin: User, val availableDiskSize: Long, val totalDiskSize: Long)
