package com.winsun.fruitmix.newdesign201804.equipment.list

import android.content.Context
import com.winsun.fruitmix.R

enum class EquipmentType {
    CLOUD_CONNECTED, CLOUD_UNCONNECTED, POWER_OFF, OFFLINE, UNDER_REVIEW, DISK_ABNORMAL, DEFAULT
}

data class EquipmentItem(var equipment_TYPE: EquipmentType = EquipmentType.DEFAULT, var name: String)

fun getEquipmentTypeStr(context: Context, equipmentTYPE: EquipmentType): String {

    return when (equipmentTYPE) {
        EquipmentType.CLOUD_CONNECTED -> ""
        EquipmentType.CLOUD_UNCONNECTED -> context.getString(R.string.login_lan)
        EquipmentType.DISK_ABNORMAL -> context.getString(R.string.disk_abnormal)
        else -> ""
    }

}

fun getEquipmentTypeIconId( equipmentTYPE: EquipmentType): Int {

    return when (equipmentTYPE) {
        EquipmentType.CLOUD_CONNECTED -> R.drawable.white_cloud
        EquipmentType.CLOUD_UNCONNECTED -> R.drawable.ic_cloud_off_white_24dp
        EquipmentType.DISK_ABNORMAL -> R.drawable.ic_cloud_off_white_24dp
        else -> R.drawable.ic_cloud_off_white_24dp
    }

}