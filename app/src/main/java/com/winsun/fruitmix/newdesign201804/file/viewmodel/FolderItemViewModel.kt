package com.winsun.fruitmix.newdesign201804.file.viewmodel

import com.winsun.fruitmix.R

open class FolderItemViewModel: FileItemViewModel(){

    init {
        fileTypeResID.set(R.drawable.ic_folder)
    }

}