package com.winsun.fruitmix.newdesign201804.mainpage

import android.content.Intent
import com.winsun.fruitmix.interfaces.Page

interface MainPage : Page {

    fun useDefaultBackPressFunction(): Boolean
    fun onBackPressed()

    fun getDrawerTitle(): String
    fun getDrawerItems(): List<DrawerItem>

    fun toggleOrientation()

    fun handleMoreIvClick()

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun quitSelectMode()

}