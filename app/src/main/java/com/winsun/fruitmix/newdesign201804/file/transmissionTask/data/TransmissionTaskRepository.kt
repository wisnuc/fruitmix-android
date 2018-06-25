package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.BaseDataRepository
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.ConflictSubTaskPolicy
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.TransmissionTask
import com.winsun.fruitmix.thread.manage.ThreadManager

class TransmissionTaskRepository(val taskManager: TaskManager, val transmissionTaskDataSource: TransmissionTaskDataSource,
                                 threadManager: ThreadManager)
    : BaseDataRepository(threadManager), TransmissionTaskDataSource {


    override fun getAllTransmissionTasks(baseLoadDataCallback: BaseLoadDataCallback<Task>) {

        val tasks = taskManager.getAllTasks()

        val runOnMainThreadCallback = createLoadCallbackRunOnMainThread(baseLoadDataCallback)

        mThreadManager.runOnCacheThread({

            transmissionTaskDataSource.getAllTransmissionTasks(object : BaseLoadDataCallback<Task> {
                override fun onFail(operationResult: OperationResult?) {
                    runOnMainThreadCallback.onSucceed(tasks, OperationSuccess())
                }

                override fun onSucceed(data: MutableList<Task>?, operationResult: OperationResult?) {

                    val newTasks = mutableListOf<Task>()

                    newTasks.addAll(data!!.filter { !taskManager.checkTaskExist(it) })
                    newTasks.addAll(tasks)

                    runOnMainThreadCallback.onSucceed(newTasks, OperationSuccess())

                }
            })

        })

    }

    override fun getTransmissionTask(taskUUID: String, baseOperateDataCallback: BaseOperateDataCallback<Task>) {

        mThreadManager.runOnCacheThread {
            transmissionTaskDataSource.getTransmissionTask(taskUUID,createOperateCallbackRunOnMainThread(baseOperateDataCallback))
        }

    }

    override fun getTransmissionTaskInCache(taskUUID: String): Task? {
        return taskManager.getTask(taskUUID)
    }

    override fun addTransmissionTask(task: Task): Boolean {

        if (task is TransmissionTask)
            task.startRefresh(this)

        return taskManager.addTask(task)

    }

    override fun deleteTransmissionTask(task: Task, baseOperateCallback: BaseOperateCallback) {
        val result = taskManager.deleteTask(task)

        if (result)
            baseOperateCallback.onSucceed()
        else {

            mThreadManager.runOnCacheThread({
                transmissionTaskDataSource.deleteTransmissionTask(task, createOperateCallbackRunOnMainThread(baseOperateCallback))
            })

        }

    }

    override fun updateConflictSubTask(taskUUID: String, nodeUUID: String, sameSourceConflictSubTaskPolicy: ConflictSubTaskPolicy,
                                       diffSourceConflictSubTaskPolicy: ConflictSubTaskPolicy,
                                       applyToAll: Boolean, baseOperateCallback: BaseOperateCallback) {

        mThreadManager.runOnCacheThread {
            transmissionTaskDataSource.updateConflictSubTask(taskUUID,nodeUUID,sameSourceConflictSubTaskPolicy,
                    diffSourceConflictSubTaskPolicy,applyToAll,
                    createOperateCallbackRunOnMainThread(baseOperateCallback))
        }

    }


}