package com.winsun.fruitmix.newdesign201804.file.sharedFolder

import android.app.Activity
import android.content.Context
import android.opengl.Visibility
import android.view.View
import com.winsun.fruitmix.interfaces.BaseView

interface SharedFolderView:BaseView {

    fun getActivity():Activity

    fun getRootView(): View

    fun setAddFabVisibility(visibility: Int)

}