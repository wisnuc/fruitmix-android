package com.winsun.fruitmix.newdesign201804.file.list.viewmodel

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt

open class FileItemViewModel(private val doHandleMoreBtnOnClick:()->Unit) {

    val isSelectMode = ObservableBoolean()
    val isSelected = ObservableBoolean()

    val folderName = ObservableField<String>()
    val fileTypeResID = ObservableInt()

    val fileFormatTime = ObservableField<String>()
    val fileFormatSize = ObservableField<String>()

    fun handleMoreBtnOnClick(){
        doHandleMoreBtnOnClick()
    }

}