package com.winsun.fruitmix.newdesign201804.file.list

interface FilePageSelectActionListener {

    fun notifySelectModeChange(isEnterSelectMode:Boolean)
    fun notifySelectCountChange(selectCount:Int)

}