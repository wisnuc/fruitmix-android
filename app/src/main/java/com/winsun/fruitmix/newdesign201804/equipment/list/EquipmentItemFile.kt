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
        EquipmentType.OFFLINE -> context.getString(R.string.station_offline)
        EquipmentType.POWER_OFF -> context.getString(R.string.shutdown)
        EquipmentType.UNDER_REVIEW -> context.getString(R.string.under_review)
        else -> ""
    }

}

fun getEquipmentTypeIconId(equipmentTYPE: EquipmentType): Int {

    return when (equipmentTYPE) {
        EquipmentType.CLOUD_CONNECTED -> R.drawable.cloud_connected
        EquipmentType.CLOUD_UNCONNECTED -> R.drawable.lan_login_corner
        EquipmentType.DISK_ABNORMAL -> R.drawable.disk_warning_corner
        EquipmentType.OFFLINE -> R.drawable.cloud_unconnected
        EquipmentType.UNDER_REVIEW -> R.drawable.under_review
        EquipmentType.POWER_OFF -> R.drawable.cloud_unconnected
        else -> R.drawable.cloud_unconnected
    }

}