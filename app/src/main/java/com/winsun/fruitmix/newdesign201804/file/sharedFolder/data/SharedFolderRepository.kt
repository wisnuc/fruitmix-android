package com.winsun.fruitmix.newdesign201804.file.sharedFolder.data

import com.winsun.fruitmix.model.BaseDataRepository
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.user.User

class SharedFolderRepository(threadManager: ThreadManager, private val sharedFolderDataSource: SharedFolderDataSource)
    : BaseDataRepository(threadManager), SharedFolderDataSource {


    override fun createSharedDisk(sharedDiskName: String, users: List<User>, baseOperateDataCallback: BaseOperateDataCallback<AbstractRemoteFile>) {

        mThreadManager.runOnCacheThread({
            sharedFolderDataSource.createSharedDisk(sharedDiskName, users, createOperateCallbackRunOnMainThread(baseOperateDataCallback))
        })

    }

    override fun deleteSharedDisk(sharedDiskUUID: String, baseOperateCallback: BaseOperateCallback) {

        mThreadManager.runOnCacheThread({
            sharedFolderDataSource.deleteSharedDisk(sharedDiskUUID, createOperateCallbackRunOnMainThread(baseOperateCallback))
        })

    }

    override fun updateSharedDiskName(sharedDiskUUID:String,newName: String, baseOperateCallback: BaseOperateCallback) {
        mThreadManager.runOnCacheThread {
            sharedFolderDataSource.updateSharedDiskName(sharedDiskUUID,newName,createOperateCallbackRunOnMainThread(baseOperateCallback))
        }
    }

    override fun updateSharedDiskWriteList(sharedDiskUUID:String,userUUIDs:List<String>, baseOperateCallback: BaseOperateCallback) {
        mThreadManager.runOnCacheThread {
            sharedFolderDataSource.updateSharedDiskWriteList(sharedDiskUUID,userUUIDs,createOperateCallbackRunOnMainThread(baseOperateCallback))
        }
    }

}