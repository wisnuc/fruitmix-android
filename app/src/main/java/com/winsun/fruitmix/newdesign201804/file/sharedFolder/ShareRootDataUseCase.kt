package com.winsun.fruitmix.newdesign201804.file.sharedFolder

import android.content.Context
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteBuiltInDrive
import com.winsun.fruitmix.file.data.model.RemotePublicDrive
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource

class ShareRootDataUseCase(private val fileDataSource: FileDataSource, val currentUserUUID: String, val context: Context) {

    private val currentFolderItems = mutableListOf<AbstractRemoteFile>()

    fun getRoot(baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>) {

        fileDataSource.getRootDrive(object : BaseLoadDataCallback<AbstractRemoteFile> {
            override fun onFail(operationResult: OperationResult?) {
                baseLoadDataCallback.onFail(operationResult)
            }

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                handleRoot(data!!, baseLoadDataCallback)
            }
        })

    }

    private var getFileCount = 0
    private var totalCount = 0

    private fun resetCount() {
        getFileCount = 0
        totalCount = 0
    }

    private fun handleRoot(data: MutableList<AbstractRemoteFile>, baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>) {

        resetCount()
        currentFolderItems.clear()

        val filterData = data.filter {

            if (it is RemotePublicDrive) {
                it.writeList.contains(currentUserUUID)
            } else it is RemoteBuiltInDrive

        }

        totalCount = filterData.size

        filterData.forEach {

            if (it is RemotePublicDrive) {

                val publicRootUUID = it.uuid

                fileDataSource.getFile(publicRootUUID, publicRootUUID, object : BaseLoadDataCallback<AbstractRemoteFile> {
                    override fun onFail(operationResult: OperationResult?) {

                        generateRootFolder(baseLoadDataCallback)

                    }

                    override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                        data?.forEach {
                            it.rootFolderUUID = publicRootUUID
                        }

                        if (data != null) {
                            currentFolderItems.addAll(data)
                        }

                        generateRootFolder(baseLoadDataCallback)

                    }
                })


            } else if (it is RemoteBuiltInDrive) {

                it.name = context.getString(R.string.built_in_drive)

                it.rootFolderUUID = it.uuid

                currentFolderItems.add(it)

                generateRootFolder(baseLoadDataCallback)

            }

        }

    }

    private fun generateRootFolder(baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>) {

        getFileCount++

        if (getFileCount == totalCount)
            baseLoadDataCallback.onSucceed(currentFolderItems, OperationSuccess())

    }

}