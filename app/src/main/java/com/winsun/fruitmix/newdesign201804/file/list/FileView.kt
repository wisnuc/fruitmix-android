package com.winsun.fruitmix.newdesign201804.file.list

import com.winsun.fruitmix.newdesign201804.file.operation.FileOperationView

interface FileView :FileOperationView {

    fun enterFileBrowserActivity()

    fun setFileRecyclerViewVisibility(visibility:Int)

}