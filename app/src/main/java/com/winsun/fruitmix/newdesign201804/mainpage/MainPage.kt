package com.winsun.fruitmix.newdesign201804.mainpage

import com.winsun.fruitmix.interfaces.Page
import com.winsun.fruitmix.mainpage.MainPagePresenterImpl

interface MainPage : Page {

    fun useDefaultBackPressFunction(): Boolean
    fun onBackPressed()

    fun getDrawerTitle(): String
    fun getDrawerItems(): List<DrawerItem>

    fun toggleOrientation()
    fun handleMoreIvClick()

    fun quitSelectMode()

}