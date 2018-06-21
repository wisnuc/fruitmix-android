package com.winsun.fruitmix.newdesign201804.file.sharedFolder.data

import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.user.User

interface SharedFolderDataSource {

    fun createSharedDisk(sharedDiskName: String,users: List<User>, baseOperateDataCallback: BaseOperateDataCallback<AbstractRemoteFile>)

    fun deleteSharedDisk(sharedDiskUUID:String,baseOperateCallback: BaseOperateCallback)

    fun updateSharedDiskName(sharedDiskUUID:String,newName:String,baseOperateCallback: BaseOperateCallback)

    fun updateSharedDiskWriteList(sharedDiskUUID:String,userUUIDs:List<String>,baseOperateCallback: BaseOperateCallback)

}