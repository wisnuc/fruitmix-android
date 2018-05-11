package com.winsun.fruitmix.newdesign201804.file.list.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.util.Util

object FakeFileDataSource : FileDataSource {

    private val abstractRemoteFiles = mutableListOf<AbstractRemoteFile>()

    private val abstractRemoteFileMap = mutableMapOf<String, AbstractFile>()

    init {

        val abstractRemoteFile = RemoteFolder()
        abstractRemoteFile.name = "test1"
        abstractRemoteFile.time = 1525404501
        abstractRemoteFile.size = 10 * 1024 * 1024
        abstractRemoteFile.uuid = Util.createLocalUUid()

        abstractRemoteFiles.add(abstractRemoteFile)

        abstractRemoteFileMap[abstractRemoteFile.uuid] = abstractRemoteFile

        val abstractRemoteFile2 = RemoteFolder()
        abstractRemoteFile2.name = "test2"
        abstractRemoteFile2.time = 1525404501
        abstractRemoteFile2.size = 10 * 1024 * 1024
        abstractRemoteFile2.uuid = Util.createLocalUUid()

        abstractRemoteFiles.add(abstractRemoteFile2)

        abstractRemoteFileMap[abstractRemoteFile2.uuid] = abstractRemoteFile2

        val abstractRemoteFile3 = RemoteFolder()
        abstractRemoteFile3.name = "test3"
        abstractRemoteFile3.time = 1525404501
        abstractRemoteFile3.size = 10 * 1024 * 1024
        abstractRemoteFile3.uuid = Util.createLocalUUid()

        abstractRemoteFiles.add(abstractRemoteFile3)

        abstractRemoteFileMap[abstractRemoteFile3.uuid] = abstractRemoteFile3

        val abstractRemoteFile4 = RemoteFile()
        abstractRemoteFile4.name = "test4.xlsx"
        abstractRemoteFile4.time = 1525404501
        abstractRemoteFile4.size = 10 * 1024 * 1024
        abstractRemoteFile4.uuid = Util.createLocalUUid()

        abstractRemoteFiles.add(abstractRemoteFile4)

        abstractRemoteFileMap[abstractRemoteFile4.uuid] = abstractRemoteFile4

        val abstractRemoteFile5 = RemoteFile()
        abstractRemoteFile5.name = "test5.doc"
        abstractRemoteFile5.time = 1525404501
        abstractRemoteFile5.size = 10 * 1024 * 1024
        abstractRemoteFile5.uuid = Util.createLocalUUid()

        abstractRemoteFiles.add(abstractRemoteFile5)

        abstractRemoteFileMap[abstractRemoteFile5.uuid] = abstractRemoteFile5

        val abstractRemoteFile6 = RemoteFile()
        abstractRemoteFile6.name = "test6.ppt"
        abstractRemoteFile6.time = 1525404501
        abstractRemoteFile6.size = 10 * 1024 * 1024
        abstractRemoteFile6.uuid = Util.createLocalUUid()

        abstractRemoteFiles.add(abstractRemoteFile6)

        abstractRemoteFileMap[abstractRemoteFile6.uuid] = abstractRemoteFile6

        val abstractRemoteFile7 = RemoteFile()
        abstractRemoteFile7.name = "test7.jpg"
        abstractRemoteFile7.time = 1525404501
        abstractRemoteFile7.size = 10 * 1024 * 1024
        abstractRemoteFile7.uuid = Util.createLocalUUid()

        abstractRemoteFiles.add(abstractRemoteFile7)

        abstractRemoteFileMap[abstractRemoteFile7.uuid] = abstractRemoteFile7

    }

    override fun getFile(baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>) {

        baseLoadDataCallback.onSucceed(abstractRemoteFiles, OperationSuccess())

    }

    override fun getFileByUUID(uuid: String): AbstractFile? {

        return abstractRemoteFileMap[uuid]

    }

}