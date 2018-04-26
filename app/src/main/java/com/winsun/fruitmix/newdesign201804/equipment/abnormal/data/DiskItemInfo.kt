package com.winsun.fruitmix.newdesign201804.equipment.abnormal.data

enum class DiskState{
    NORMAL,LOST,NEW_AVAILABLE
}

data class DiskItemInfo(val diskState: DiskState,val brand:String,val totalSize:Double,val serialNumber:String)