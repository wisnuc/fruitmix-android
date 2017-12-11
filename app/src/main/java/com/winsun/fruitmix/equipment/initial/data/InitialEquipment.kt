package com.winsun.fruitmix.equipment.initial.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.equipment.initial.viewmodel.DiskVolumeViewModel
import com.winsun.fruitmix.user.User

/**
 * Created by Administrator on 2017/12/7.
 */

data class EquipmentDiskVolume(val model: String, val name: String, val size: Long, val interfaceType: String, val state: String, val instruction: String,
                               val removable: Boolean)

interface InitialEquipmentDataSource {

    fun getStorageInfo(ip: String, callback: BaseLoadDataCallback<EquipmentDiskVolume>)

    fun installSystem(ip:String,userName: String, userPassword: String, mode: String, diskVolumeViewModels: List<DiskVolumeViewModel>,
                      callback: BaseOperateDataCallback<User>)

}

interface ShowFirstInitialEquipmentInfoListener {

    fun showEquipmentInDialog(diskVolumeViewModel: DiskVolumeViewModel)

}

interface SelectDiskModeListener {

    fun selectDiskMode()

}





