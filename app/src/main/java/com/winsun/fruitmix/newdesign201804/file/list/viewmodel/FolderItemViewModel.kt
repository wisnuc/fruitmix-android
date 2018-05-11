package com.winsun.fruitmix.newdesign201804.file.list.viewmodel

import com.winsun.fruitmix.R
import com.winsun.fruitmix.file.data.model.AbstractFile

open class FolderItemViewModel(abstractFile: AbstractFile,doHandleMoreBtnOnClick:()->Unit = {}): FileItemViewModel(abstractFile,doHandleMoreBtnOnClick) {

    init {
        fileTypeResID.set(R.drawable.ic_folder)
    }

}