package com.winsun.fruitmix.newdesign201804.file.list.viewmodel

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.util.FileUtil

open class FileItemViewModel(val abstractFile: AbstractFile,var doHandleMoreBtnOnClick: () -> Unit = {}) {

    val isSelectMode = ObservableBoolean()

    val isSelected = ObservableBoolean()

    val folderName = ObservableField<String>()
    val fileTypeResID = ObservableInt()

    val fileFormatTime = ObservableField<String>()
    val fileFormatSize = ObservableField<String>()

    val showOfflineAvailableIv = ObservableBoolean(false)
    val showMoreBtn = ObservableBoolean(true)

    val isDisable = ObservableBoolean(false)

    init {

        fileTypeResID.set(abstractFile.fileTypeResID)
        fileFormatSize.set(FileUtil.formatFileSize(abstractFile.size))
        fileFormatTime.set(abstractFile.timeText)
        folderName.set(abstractFile.name)

    }

    fun handleMoreBtnOnClick() {
        doHandleMoreBtnOnClick()
    }

}