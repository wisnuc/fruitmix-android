package com.winsun.fruitmix.newdesign201804.file.list.data

import android.support.v7.widget.SwitchCompat
import android.widget.Switch
import com.winsun.fruitmix.command.AbstractCommand
import com.winsun.fruitmix.model.BottomMenuItem

class FileWithSwitchBottomItem(iconResID: Int, text: String, command: AbstractCommand,
                               val switchOnClick: (bottomMenuItem: FileWithSwitchBottomItem) -> Unit) : BottomMenuItem(iconResID, text, command) {

    fun isShowSwitchBtn(): Boolean {
        return true
    }

    private var switchEnableState = false

    fun setSwitchEnableState(switchEnableState: Boolean) {
        this.switchEnableState = switchEnableState
    }

    fun isSwitchEnabled(): Boolean {
        return switchEnableState
    }

}