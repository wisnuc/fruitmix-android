package com.winsun.fruitmix.model

import com.winsun.fruitmix.command.AbstractCommand
import com.winsun.fruitmix.command.BaseAbstractCommand

class DivideBottomMenuItem(iconResID: Int = 0, text: String = "", abstractCommand: AbstractCommand = BaseAbstractCommand())
    : BottomMenuItem(iconResID, text, abstractCommand) {

    override fun handleOnClickEvent() {

    }

}