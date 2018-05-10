package com.winsun.fruitmix.newdesign201804.file.offlineFile.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.file.data.model.AbstractLocalFile
import com.winsun.fruitmix.file.data.model.LocalFile
import com.winsun.fruitmix.file.data.model.LocalFolder
import com.winsun.fruitmix.model.operationResult.OperationSuccess

class FakeOfflineFileDataSource : OfflineFileDataSource {

    override fun getFile(baseLoadDataCallback: BaseLoadDataCallback<AbstractLocalFile>) {

        val abstractLocalFiles = mutableListOf<AbstractLocalFile>()

        val localFolder1 = LocalFolder()

        localFolder1.name = "新建文件夹1"
        localFolder1.time = 1525860230L
        localFolder1.size = 42467328L

        abstractLocalFiles.add(localFolder1)

        val localFolder2 = LocalFolder()

        localFolder2.name = "新建文件夹2"
        localFolder2.time = 1525860020L
        localFolder2.size = 31981568L

        abstractLocalFiles.add(localFolder2)

        val localFolder3 = LocalFile()

        localFolder3.name = "未命名.ppt"
        localFolder3.time = 1525460020L
        localFolder3.size = 21495808L

        abstractLocalFiles.add(localFolder3)

        val localFolder4 = LocalFile()

        localFolder4.name = "未命名.docx"
        localFolder4.time = 1525260020L
        localFolder4.size = 21195808L

        abstractLocalFiles.add(localFolder4)

        baseLoadDataCallback.onSucceed(abstractLocalFiles, OperationSuccess())

    }

}