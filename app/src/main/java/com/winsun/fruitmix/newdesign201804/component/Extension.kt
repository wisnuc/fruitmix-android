package com.winsun.fruitmix.newdesign201804.component

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource
import com.winsun.fruitmix.user.User
import com.winsun.fruitmix.user.datasource.InjectUser

fun ViewGroup.inflateView(resource: Int): View {
    return LayoutInflater.from(context).inflate(resource, this, false)
}

fun Context.getCurrentUserUUID():String{
    return  InjectSystemSettingDataSource.provideSystemSettingDataSource(this)
            .currentLoginUserUUID
}

fun Context.getCurrentUserHome(): String {

    val currentUser = InjectUser.provideRepository(this).getUserByUUID(getCurrentUserUUID())

    return currentUser.home

}