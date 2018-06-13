package com.winsun.fruitmix.newdesign201804.component

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam
import com.winsun.fruitmix.file.data.download.param.FileFromStationFolderDownloadParam
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFolder
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

fun AbstractRemoteFile.createFileDownloadParam(): FileDownloadParam {
    return FileFromStationFolderDownloadParam(this.uuid,
            this.parentFolderUUID, this.rootFolderUUID, this.name)
}

