package com.winsun.fruitmix.newdesign201804.file.list.data

import com.winsun.fruitmix.command.AbstractCommand
import com.winsun.fruitmix.model.BottomMenuItem

class FileWithSwitchBottomItem(iconResID:Int,text:String,command:AbstractCommand,
                               val switchOnClick:(bottomMenuItem:BottomMenuItem)->Unit):BottomMenuItem(iconResID,text,command) {

    fun isShowSwitchBtn(): Boolean {
        return true
    }

}