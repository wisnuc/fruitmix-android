package com.winsun.fruitmix.newdesign201804.file.list

import android.app.Activity
import com.winsun.fruitmix.newdesign201804.file.list.operation.FileOperationView
import com.winsun.fruitmix.newdesign201804.mainpage.MainPage

interface FileView :FileOperationView {

    fun enterFileBrowserActivity()

}