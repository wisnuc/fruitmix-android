package com.winsun.fruitmix.newdesign201804.file.list

interface FilePageActionListener {

    fun notifyFolderLevelChanged(isRootFolder: Boolean, folderName: String = "",folderUUID:String = "")

}