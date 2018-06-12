package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.file.transmission.model.Transmission
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.BTTask
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.BTTaskParam
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl

object TransmissionTaskRepository : TransmissionTaskDataSource {

    private val tasks = mutableListOf<Task>()

    override fun getAllTransmissionTasks(baseLoadDataCallback: BaseLoadDataCallback<Task>) {

        baseLoadDataCallback.onSucceed(tasks, OperationSuccess())

    }

    override fun addTransmissionTask(task: Task, baseOperateCallback: BaseOperateCallback) {
        tasks.add(task)

        baseOperateCallback.onSucceed()
    }

    override fun deleteTransmissionTask(task: Task, baseOperateCallback: BaseOperateCallback) {
        tasks.remove(task)

        baseOperateCallback.onSucceed()
    }

}