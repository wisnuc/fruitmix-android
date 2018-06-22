package com.winsun.fruitmix.newdesign201804.file.sharedFolder

import android.view.View
import com.winsun.fruitmix.interfaces.BaseView
import com.winsun.fruitmix.newdesign201804.file.operation.FileOperationView

interface SharedFolderView:BaseView,FileOperationView {

    fun getViewForSnackBar(): View

    fun setAddFabVisibility(visibility: Int)

}