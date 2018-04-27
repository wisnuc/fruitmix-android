package com.winsun.fruitmix.newdesign201804.equipment.abnormal.strategy

import android.content.Context
import android.support.v7.app.AlertDialog
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.equipment.abnormal.data.DiskState
import com.winsun.fruitmix.newdesign201804.equipment.add.data.DiskMode
import com.winsun.fruitmix.newdesign201804.equipment.model.DiskAbnormalEquipmentItem

interface HandleAbnormalEquipmentStrategy {

    fun canContinueUse(): Boolean {
        return false
    }

    fun canRepair(): Boolean {
        return false
    }

    fun onContinueUseClick(context: Context) {}
    fun onRepairClick(context: Context) {}

}

class HandleAbnormalEquipmentStrategyFactory(private val abnormalEquipmentItem: DiskAbnormalEquipmentItem) {

    fun generateStrategy(): HandleAbnormalEquipmentStrategy {

        when (abnormalEquipmentItem.diskMode) {

            DiskMode.SINGLE -> {

                return if (abnormalEquipmentItem.diskItemInfos.filter { it.diskState == DiskState.LOST }.size == abnormalEquipmentItem.diskItemInfos.size)
                    SingleModeDiskAllLostStrategy()
                else
                    SingleModeDiskLostPartStrategy()
            }

            DiskMode.RAID1 -> {

                val lostDiskSize = abnormalEquipmentItem.diskItemInfos.filter { it.diskState == DiskState.LOST }.size
                val newAvailableDiskSize = abnormalEquipmentItem.diskItemInfos.filter { it.diskState == DiskState.NEW_AVAILABLE }.size

                if (lostDiskSize > 1 && newAvailableDiskSize >= 1) {
                    return Raid1ModeLostTwoMoreDiskNewDiskAvailableStrategy()
                } else if (lostDiskSize == 1 && newAvailableDiskSize >= 1)
                    return Raid1ModeLostOneDiskAndNewDiskAvailableStrategy()
                else if (lostDiskSize > 1 && newAvailableDiskSize == 0)
                    return Raid1ModeLostTwoMoreDiskStrategy()
                else if (lostDiskSize == 1 && newAvailableDiskSize == 0)
                    return Raid1ModeLostOneDiskStrategy()
                else
                    return SingleModeDiskAllLostStrategy()

            }

        }

    }

}

class SingleModeDiskAllLostStrategy : HandleAbnormalEquipmentStrategy

class SingleModeDiskLostPartStrategy : HandleAbnormalEquipmentStrategy {

    override fun canContinueUse(): Boolean {
        return true
    }

    override fun onContinueUseClick(context: Context) {

        AlertDialog.Builder(context)
                .setTitle(R.string.continue_to_use_dialog_title)
                .setMessage(R.string.lost_disk_data_not_retrieve)
                .setPositiveButton(R.string.yes, null)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .create().show()

    }

}

open class Raid1ModeLostOneDiskStrategy : HandleAbnormalEquipmentStrategy {

    override fun canContinueUse(): Boolean {
        return true
    }

    override fun onContinueUseClick(context: Context) {

        AlertDialog.Builder(context)
                .setTitle(R.string.continue_to_use_dialog_title)
                .setMessage(R.string.lost_disk_data_exist)
                .setPositiveButton(R.string.yes, null)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .create().show()

    }

}

class Raid1ModeLostOneDiskAndNewDiskAvailableStrategy : Raid1ModeLostOneDiskStrategy() {

    override fun canContinueUse(): Boolean {
        return true
    }

    override fun canRepair(): Boolean {
        return true
    }

    override fun onRepairClick(context: Context) {

        AlertDialog.Builder(context)
                .setTitle(R.string.repair_immediately_dialog_title)
                .setMessage(R.string.raid1_repair)
                .setPositiveButton(R.string.yes, null)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .create().show()

    }

}

open class Raid1ModeLostTwoMoreDiskStrategy : HandleAbnormalEquipmentStrategy {

    override fun canContinueUse(): Boolean {
        return true
    }

    override fun onContinueUseClick(context: Context) {

        AlertDialog.Builder(context)
                .setTitle(R.string.continue_to_use_dialog_title)
                .setMessage(R.string.lost_disk_data_not_retrieve)
                .setPositiveButton(R.string.yes, null)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .create().show()

    }

}

class Raid1ModeLostTwoMoreDiskNewDiskAvailableStrategy : Raid1ModeLostTwoMoreDiskStrategy() {

    override fun canRepair(): Boolean {
        return true
    }

    override fun onRepairClick(context: Context) {

        AlertDialog.Builder(context)
                .setTitle(R.string.repair_dialog_title)
                .setMessage(R.string.raid1_lost_disk_data_not_retrieve)
                .setPositiveButton(R.string.yes, null)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .create().show()

    }

}






