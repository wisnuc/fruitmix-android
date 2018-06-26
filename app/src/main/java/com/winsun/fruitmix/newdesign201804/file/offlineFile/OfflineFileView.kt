package com.winsun.fruitmix.newdesign201804.file.offlineFile

import android.view.View
import com.winsun.fruitmix.interfaces.BaseView

interface OfflineFileView:BaseView {

    fun getRootView(): View

    fun setTitle(title:String)

}