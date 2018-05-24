package com.winsun.fruitmix.newdesign201804.share

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.mainpage.DrawerItem
import com.winsun.fruitmix.newdesign201804.mainpage.MainPage

class SharePage(val activity: Activity) : MainPage {

    private val sharePageView = LayoutInflater.from(activity).inflate(R.layout.share_page, null, false)

    override fun useDefaultBackPressFunction(): Boolean {
        return true
    }

    override fun onBackPressed() {
    }

    override fun getDrawerTitle(): String {
        return activity.getString(R.string.share_text)
    }

    override fun getDrawerItems(): List<DrawerItem> {
        return emptyList()
    }

    override fun getView(): View {
        return sharePageView
    }

    override fun refreshView() {

    }

    override fun refreshViewForce() {

    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {

    }

    override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {

    }

    override fun canEnterSelectMode(): Boolean {
        return false
    }

    override fun toggleOrientation() {

    }

    override fun handleMoreIvClick() {

    }

    override fun quitSelectMode() {

    }

}