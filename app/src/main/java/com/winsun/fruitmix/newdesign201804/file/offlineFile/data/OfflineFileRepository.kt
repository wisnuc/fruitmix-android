package com.winsun.fruitmix.newdesign201804.file.offlineFile.data

import com.winsun.fruitmix.BaseDataRepository
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.file.data.model.AbstractLocalFile
import com.winsun.fruitmix.file.data.model.LocalFile
import com.winsun.fruitmix.file.data.model.LocalFolder
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.util.FileUtil
import java.io.File

class OfflineFileRepository(threadManager: ThreadManager) : BaseDataRepository(threadManager), OfflineFileDataSource {

    override fun getFile(folderPath: String, filterPaths: List<String>, baseLoadDataCallback: BaseLoadDataCallback<AbstractLocalFile>) {

        mThreadManager.runOnCacheThread({
            doGetFile(folderPath, filterPaths, createLoadCallbackRunOnMainThread(baseLoadDataCallback))
        })

    }

    private fun doGetFile(folderPath: String, filterPaths: List<String>, baseLoadDataCallback: BaseLoadDataCallback<AbstractLocalFile>) {
        val files = File(folderPath).listFiles()

        val abstractLocalFiles = mutableListOf<AbstractLocalFile>()

        if (files != null) {

            val fileLists = files.filter {

                val file = it

                !(filterPaths.any {

                    file.path.contains(it)

                })

            }

            for (file in fileLists) {

                val abstractLocalFile: AbstractLocalFile = if (file.isDirectory) {
                    LocalFolder()
                } else {
                    LocalFile()
                }

                abstractLocalFile.name = file.name
                abstractLocalFile.path = file.absolutePath
                abstractLocalFile.time = file.lastModified()
                abstractLocalFile.size = file.length()

                abstractLocalFiles.add(abstractLocalFile)

            }

        }

        baseLoadDataCallback.onSucceed(abstractLocalFiles, OperationSuccess())
    }

}