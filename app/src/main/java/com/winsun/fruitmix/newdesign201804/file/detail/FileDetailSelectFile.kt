package com.winsun.fruitmix.newdesign201804.file.detail

import com.winsun.fruitmix.file.data.model.AbstractRemoteFile

object FileDetailSelectFile {

    private lateinit var selectFile:AbstractRemoteFile

    fun saveFile(file: AbstractRemoteFile){
        selectFile = file
    }

    fun getFile():AbstractRemoteFile{
        return selectFile
    }

}