package com.winsun.fruitmix.equipment.maintenance.data

/**
 * Created by Administrator on 2018/1/2.
 */


data class VolumeState(val position:Int,val type:String,val mode:String,val uuid:String,val isMounted:Boolean,
                       val noMissing:Boolean,var lastSystem:Boolean,val fruitmixOK:Boolean,val userOK:Boolean)
