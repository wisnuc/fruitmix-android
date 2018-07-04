package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.db.DBUtils
import com.winsun.fruitmix.file.data.station.StationFileRepository
import com.winsun.fruitmix.model.operationResult.OperationSQLException
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.ConflictSubTaskPolicy
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.DownloadTask
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.UploadTask
import com.winsun.fruitmix.thread.manage.ThreadManager

class TransmissionTaskDBDataSource(val dbUtils: DBUtils, val fileDataSource: FileDataSource,
                                   val threadManager: ThreadManager,
                                   val stationFileRepository: StationFileRepository,
                                   val currentUserUUID: String) : TransmissionTaskDataSource {

    override fun getAllTransmissionTasks(baseLoadDataCallback: BaseLoadDataCallback<Task>) {

        val tasks = mutableListOf<Task>()

        tasks.addAll(dbUtils.getAllDownloadTasks(currentUserUUID, fileDataSource, threadManager, stationFileRepository))
        tasks.addAll(dbUtils.getAllUploadTasks(currentUserUUID, fileDataSource, threadManager, stationFileRepository))

        baseLoadDataCallback.onSucceed(tasks, OperationSuccess())

    }

    override fun getBaseMoveCopyTask(taskUUID: String, baseOperateDataCallback: BaseOperateDataCallback<Task>) {


    }

    override fun addTransmissionTask(task: Task): Boolean {

        val result = when (task) {
            is UploadTask -> dbUtils.insertUploadTask(task)
            is DownloadTask -> dbUtils.insertDownloadTask(task)
            else -> 1
        }

        return result > 0

    }

    override fun deleteTransmissionTask(task: Task, baseOperateCallback: BaseOperateCallback) {

        val result = when (task) {
            is UploadTask -> dbUtils.deleteUploadTask(task.uuid)
            is DownloadTask -> dbUtils.deleteDownloadTask(task.uuid)
            else -> 1
        }

        if (result > 0)
            baseOperateCallback.onSucceed()
        else
            baseOperateCallback.onFail(OperationSQLException())


    }

    override fun updateUploadDownloadTaskState(task: Task, baseOperateCallback: BaseOperateCallback) {

        val result = when (task) {
            is UploadTask -> dbUtils.uploadUploadTaskState(task.uuid, task.getCurrentState().getType().value)
            is DownloadTask -> dbUtils.uploadDownloadTaskState(task.uuid, task.getCurrentState().getType().value)
            else -> 1
        }

        if (result > 0)
            baseOperateCallback.onSucceed()
        else
            baseOperateCallback.onFail(OperationSQLException())

    }

    override fun updateConflictSubTask(taskUUID: String, nodeUUID: String, sameSourceConflictSubTaskPolicy: ConflictSubTaskPolicy, diffSourceConflictSubTaskPolicy: ConflictSubTaskPolicy, applyToAll: Boolean, baseOperateCallback: BaseOperateCallback) {

    }

}