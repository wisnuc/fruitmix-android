package com.winsun.fruitmix.newdesign201804.file.move

import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile

object SelectMoveFileDataSource {

    private val mSelectFiles = mutableListOf<AbstractFile>()

    fun saveSelectFiles(selectFiles:MutableList<AbstractFile>){
        mSelectFiles.clear()
        mSelectFiles.addAll(selectFiles)
    }

    fun getSelectFiles():MutableList<AbstractFile>{
        return mSelectFiles
    }

}