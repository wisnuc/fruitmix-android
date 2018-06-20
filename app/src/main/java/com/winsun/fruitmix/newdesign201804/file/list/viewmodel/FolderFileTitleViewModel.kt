package com.winsun.fruitmix.newdesign201804.file.list.viewmodel

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.winsun.fruitmix.R


class FolderFileTitleViewModel {

    val showSortBtn: ObservableBoolean = ObservableBoolean()
    val isFolder: ObservableBoolean = ObservableBoolean()
    val sortModeText: ObservableInt = ObservableInt()
    val sortDirectionIconResID: ObservableInt = ObservableInt(R.drawable.green_up_arrow)

}
