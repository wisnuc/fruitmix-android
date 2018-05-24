package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.file.data.download.param.FileFromStationFolderDownloadParam
import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.*
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl
import com.winsun.fruitmix.util.Util

object FakeTransmissionTaskRepository : TransmissionTaskDataSource {

    private val tasks = mutableListOf<Task>()

    init {

        val ppt = RemoteFile()
        ppt.name = "我的文件选择.ppt"
        ppt.size = 132070243L
        ppt.uuid = Util.createLocalUUid()

        tasks.add(BTTask(ppt, ThreadManagerImpl.getInstance(), BTTaskParam("")))

        val word = RemoteFile()
        word.name = "呵呵.doc"
        word.uuid = Util.createLocalUUid()
        word.size = 1320743L

        tasks.add(MoveTask(word, ThreadManagerImpl.getInstance(), MoveTaskParam("")))

        val xlsx = RemoteFile()
        xlsx.uuid = Util.createLocalUUid()
        xlsx.name = "哈哈.xlsx"
        xlsx.size = 1070243L

        tasks.add(CopyTask(xlsx, ThreadManagerImpl.getInstance(), CopyTaskParam("")))

        val smb = RemoteFile()
        smb.uuid = Util.createLocalUUid()
        smb.name = "伊艾儿.txt"
        smb.size = 1320703L

        tasks.add(SMBTask(smb, ThreadManagerImpl.getInstance(), SMBTaskParam("")))

    }

    override fun getAllTransmissionTasks(baseLoadDataCallback: BaseLoadDataCallback<Task>) {

        return baseLoadDataCallback.onSucceed(tasks, OperationSuccess())

    }

    override fun addTransmissionTask(task: Task, baseOperateCallback: BaseOperateCallback) {


    }

    override fun deleteTransmissionTask(task: Task, baseOperateCallback: BaseOperateCallback) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}