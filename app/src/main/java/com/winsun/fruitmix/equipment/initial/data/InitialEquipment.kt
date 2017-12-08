package com.winsun.fruitmix.equipment.initial.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback

/**
 * Created by Administrator on 2017/12/7.
 */

data class EquipmentDiskVolume(val model:String,val name:String,val size:Long,val interfaceType:String,val state:String,val instruction:String)


interface InitialEquipmentDataSource {

    fun getStorageInfo(ip:String,callback:BaseLoadDataCallback<EquipmentDiskVolume>):Nothing

}



