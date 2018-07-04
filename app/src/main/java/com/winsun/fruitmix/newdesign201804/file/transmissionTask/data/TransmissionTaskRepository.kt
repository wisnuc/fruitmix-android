package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import android.util.Log
import com.winsun.fruitmix.BaseDataRepository
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.*
import com.winsun.fruitmix.thread.manage.ThreadManager

private const val TAG = "TransmissionTaskRepo"

class TransmissionTaskRepository(private val taskManager: TaskManager, private val transmissionTaskRemoteDataSource: TransmissionTaskDataSource,
                                 private val transmissionTaskDBDataSource: TransmissionTaskDBDataSource,
                                 threadManager: ThreadManager)
    : BaseDataRepository(threadManager), TransmissionTaskDataSource {

    override fun getAllTransmissionTasks(baseLoadDataCallback: BaseLoadDataCallback<Task>) {

        val runOnMainThreadCallback = createLoadCallbackRunOnMainThread(baseLoadDataCallback)

        mThreadManager.runOnCacheThread({

            if (taskManager.isCacheDirty()) {
                transmissionTaskDBDataSource.getAllTransmissionTasks(object : BaseLoadDataCallback<Task> {
                    override fun onSucceed(data: MutableList<Task>?, operationResult: OperationResult?) {

                        data?.forEach {
                            if (!taskManager.checkTaskExist(it))
                                taskManager.addTask(it)
                        }

                        handleTaskManagerInitial(runOnMainThreadCallback)
                    }

                    override fun onFail(operationResult: OperationResult?) {

                    }
                })
            } else {

                handleTaskManagerInitial(runOnMainThreadCallback)

            }

        })

    }

    private fun handleTaskManagerInitial(runOnMainThreadCallback: BaseLoadDataCallback<Task>) {
        val tasks = taskManager.getAllTasks()

        transmissionTaskRemoteDataSource.getAllTransmissionTasks(object : BaseLoadDataCallback<Task> {
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
    }

    override fun getBaseMoveCopyTask(taskUUID: String, baseOperateDataCallback: BaseOperateDataCallback<Task>) {

        mThreadManager.runOnCacheThread {
            transmissionTaskRemoteDataSource.getBaseMoveCopyTask(taskUUID, createOperateCallbackRunOnMainThread(baseOperateDataCallback))
        }

    }

    fun getTransmissionTaskInCache(taskUUID: String): Task? {
        return taskManager.getTask(taskUUID)
    }

    fun getAllDownloadUploadTransmissionTaskInCache(): List<Task> {

        return taskManager.getAllTasks().filter {
            it is UploadTask || it is DownloadTask
        }

    }

    fun handleExitApp() {
        taskManager.handleExitApp()
    }

    override fun addTransmissionTask(task: Task): Boolean {

        task.init()

        if (task is BaseMoveCopyTask)
            task.startRefresh(this)

        val result = taskManager.addTask(task)

        if (task is DownloadTask || task is UploadTask) {

            mThreadManager.runOnCacheThread {
                val addTaskInDBResult = transmissionTaskDBDataSource.addTransmissionTask(task)
                Log.d(TAG, "addTaskInDBResult:$addTaskInDBResult")
            }

        }

        return result

    }

    override fun deleteTransmissionTask(task: Task, baseOperateCallback: BaseOperateCallback) {
        val result = taskManager.deleteTask(task)

        val runOnMainThreadCallback = createOperateCallbackRunOnMainThread(baseOperateCallback)

        if (result) {

            mThreadManager.runOnCacheThread {
                transmissionTaskDBDataSource.deleteTransmissionTask(task, runOnMainThreadCallback)
            }

        } else {

            mThreadManager.runOnCacheThread({
                transmissionTaskRemoteDataSource.deleteTransmissionTask(task, object : BaseOperateCallback {
                    override fun onSucceed() {
                        transmissionTaskDBDataSource.deleteTransmissionTask(task, runOnMainThreadCallback)
                    }

                    override fun onFail(operationResult: OperationResult?) {
                        runOnMainThreadCallback.onFail(operationResult)
                    }
                })
            })

        }

    }

    override fun updateUploadDownloadTaskState(task: Task, baseOperateCallback: BaseOperateCallback) {

        mThreadManager.runOnCacheThread {
            transmissionTaskDBDataSource.updateUploadDownloadTaskState(task, createOperateCallbackRunOnMainThread(baseOperateCallback))
        }

    }

    override fun updateConflictSubTask(taskUUID: String, nodeUUID: String, sameSourceConflictSubTaskPolicy: ConflictSubTaskPolicy,
                                       diffSourceConflictSubTaskPolicy: ConflictSubTaskPolicy,
                                       applyToAll: Boolean, baseOperateCallback: BaseOperateCallback) {

        mThreadManager.runOnCacheThread {
            transmissionTaskRemoteDataSource.updateConflictSubTask(taskUUID, nodeUUID, sameSourceConflictSubTaskPolicy,
                    diffSourceConflictSubTaskPolicy, applyToAll,
                    createOperateCallbackRunOnMainThread(baseOperateCallback))
        }

    }

}