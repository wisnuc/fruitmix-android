package com.winsun.fruitmix.newdesign201804.equipment.add

import com.winsun.fruitmix.user.User

open class BaseNewEquipmentInfo(val name: String)

class UnBoundEquipmentInfo(name: String, val unboundEquipmentDiskInfos: List<UnboundEquipmentDiskInfo>) : BaseNewEquipmentInfo(name)

class AvailableEquipmentInfo(name: String, val availableEquipmentDiskInfo: AvailableEquipmentDiskInfo) : BaseNewEquipmentInfo(name)

class ReinitializationEquipmentInfo(name: String) : BaseNewEquipmentInfo(name)

enum class DiskMode {
    SINGLE, RAID1
}

data class UnboundEquipmentDiskInfo(val originalEquipmentName: String, val diskMode: DiskMode, val availableDiskSize: Long, val totalDiskSize: Long)

data class AvailableEquipmentDiskInfo(val admin: User, val availableDiskSize: Long, val totalDiskSize: Long)
