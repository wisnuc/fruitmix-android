package com.winsun.fruitmix.newdesign201804.file.list.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.http.HttpResponse
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.TaskState
import com.winsun.fruitmix.util.Util

object FakeFileDataSource : FileDataSource {

    private val abstractRemoteFiles = mutableListOf<AbstractRemoteFile>()

    private val abstractRemoteFileMap = mutableMapOf<String, AbstractFile>()

    private val abstractRemoteFolder1UUID = Util.createLocalUUid()

    init {

        val abstractRemoteFile = RemoteFolder()
        abstractRemoteFile.name = "test1"
        abstractRemoteFile.time = 1525404501
        abstractRemoteFile.size = 10 * 1024 * 1024
        abstractRemoteFile.uuid = abstractRemoteFolder1UUID

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

    override fun getFile(rootUUID: String, folderUUID: String, baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>) {

        if (folderUUID == abstractRemoteFolder1UUID) {

            val files = mutableListOf<AbstractRemoteFile>()

            val abstractRemoteFile = RemoteFolder()
            abstractRemoteFile.name = "testLevel1"
            abstractRemoteFile.time = 1525404501
            abstractRemoteFile.size = 10 * 1024 * 1024
            abstractRemoteFile.uuid = Util.createLocalUUid()

            files.add(abstractRemoteFile)

            val abstractRemoteFile2 = RemoteFile()
            abstractRemoteFile2.name = "testLevel2"
            abstractRemoteFile2.time = 1525404501
            abstractRemoteFile2.size = 10 * 1024 * 1024
            abstractRemoteFile2.uuid = Util.createLocalUUid()

            files.add(abstractRemoteFile2)

            baseLoadDataCallback.onSucceed(files, OperationSuccess())

        } else
            baseLoadDataCallback.onSucceed(abstractRemoteFiles, OperationSuccess())

    }

    override fun getFileByUUID(uuid: String): AbstractFile? {

        return abstractRemoteFileMap[uuid]

    }

    override fun searchFile(keys: List<String>, baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>) {

        val searchResults = mutableListOf<AbstractRemoteFile>()

        abstractRemoteFiles.forEach {

            val abstractRemoteFile = it

            var checkPassCount = 0

            keys.forEach {

                if (abstractRemoteFile.name.contains(it))
                    checkPassCount++

            }

            if (checkPassCount == keys.size)
                searchResults.add(abstractRemoteFile)

        }

        baseLoadDataCallback.onSucceed(searchResults, OperationSuccess())

    }

    override fun getRootDrive(baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createFolder(folderName: String, driveUUID: String, dirUUID: String, callback: BaseOperateDataCallback<HttpResponse>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun renameFile(oldName: String, newName: String, driveUUID: String, dirUUID: String, callback: BaseOperateCallback) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun copyFile(srcFolder: AbstractRemoteFile, targetFolder: AbstractRemoteFile, entries: List<AbstractRemoteFile>, callback: BaseOperateCallback) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun moveFile(srcFolder: AbstractRemoteFile, targetFolder: AbstractRemoteFile, entries: List<AbstractRemoteFile>, callback: BaseOperateCallback) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun downloadFile(fileDownloadParam: FileDownloadParam, task: Task) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}