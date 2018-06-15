package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.BaseDataRepository
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.file.data.model.FileTaskManager
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.DownloadTask
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.util.FileUtil

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

                    data?.addAll(tasks)

                    runOnMainThreadCallback.onSucceed(data, OperationSuccess())

                }
            })

        })


    }

    override fun addTransmissionTask(task: Task, baseOperateCallback: BaseOperateCallback) {

        taskManager.addTask(task)

        baseOperateCallback.onSucceed()

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

}