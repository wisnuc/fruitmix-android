package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

import android.os.Handler
import android.os.Message
import android.util.Log
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseOperateCallbackImpl
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractLocalFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.data.FileUploadParam
import com.winsun.fruitmix.newdesign201804.file.transmission.TransmissionDataSource
import com.winsun.fruitmix.newdesign201804.file.transmission.model.Transmission
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.TransmissionTaskDataSource
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.util.FileUtil
import java.io.File
import java.util.concurrent.Future

const val TASK_UUID_KEY = "task_uuid"

const val TASK_TAG = "task"

abstract class Task(val uuid: String, val createUserUUID: String, var abstractFile: AbstractFile, val threadManager: ThreadManager, val max: Int = 100,
                    val startSpeedHandler: Boolean = true, val subTasks: MutableList<SubTask> = mutableListOf()) {

    abstract fun getTypeResID(): Int

    private lateinit var currentTaskState: TaskState

    private val taskStateObservers = mutableListOf<TaskStateObserver>()

    private var mTotalSize: String = ""

    fun setTotalSize(totalSize: String) {
        mTotalSize = totalSize
    }

    fun getTotalSize(): String {
        return mTotalSize
    }

    //after construct task,call init(),
    fun init() {

        if (!::currentTaskState.isInitialized)
            currentTaskState = InitialTaskState(this)

    }

    fun startTask() {
        currentTaskState.start()
    }

    fun pauseTask() {
        currentTaskState.pause()
    }

    fun resumeTask() {
        currentTaskState.resume()
    }

    fun restartTask() {
        currentTaskState.restart()
    }

    abstract fun executeTask()

    fun deleteTask() {

        taskStateObservers.clear()

        currentTaskState.delete()

    }

    fun cancelTask() {
        currentTaskState.cancel()
    }

    open fun doCancelTask() {

    }

    open fun doDeleteTask() {

    }

    fun registerObserver(taskStateObserver: TaskStateObserver) {
        taskStateObservers.add(taskStateObserver)
    }

    fun unregisterObserver(taskStateObserver: TaskStateObserver) {
        taskStateObservers.remove(taskStateObserver)
    }

    @Synchronized
    open fun setCurrentState(taskState: TaskState) {

        val preState = currentTaskState

        if (taskState.getType() != currentTaskState.getType()) {

            currentTaskState.onFinishState()

            taskState.onStartState()

        }

        currentTaskState = taskState

        if (currentTaskState.getType() == StateType.FINISH) {
            Log.d(TASK_TAG, "finish taskUUID: $uuid,currentTaskState = taskState fileName: ${abstractFile.name} taskStateObservers size:${taskStateObservers.size}")
        }

        threadManager.runOnMainThread {

            taskStateObservers.forEach {

                if (currentTaskState.getType() == StateType.FINISH)
                    Log.d(TASK_TAG, "finish taskUUID:$uuid,notifyStateChanged fileName: ${abstractFile.name}")

                it.notifyStateChanged(currentTaskState,preState)

            }

        }

    }

    @Synchronized
    fun getCurrentState(): TaskState {
        return currentTaskState
    }

}

interface TaskStateObserver {

    fun notifyStateChanged(currentState: TaskState,preState:TaskState)

}

open class UploadTask(uuid: String, createUserUUID: String, abstractFile: AbstractLocalFile, val fileDataSource: FileDataSource,
                      val fileUploadParam: FileUploadParam,
                      threadManager: ThreadManager) : Task(uuid, createUserUUID, abstractFile, threadManager) {

    protected lateinit var future: Future<Boolean>

    override fun getTypeResID(): Int {
        return R.drawable.upload
    }

    override fun executeTask() {

        val uploadFileCallable = OperateFileCallable {
            fileDataSource.uploadFile(fileUploadParam, this)
        }

        future = threadManager.runOnCacheThread(uploadFileCallable)

    }

    override fun doCancelTask() {
        super.doCancelTask()

        future.cancel(true)

    }

    override fun doDeleteTask() {

        super.doDeleteTask()

    }

}

private const val DOWNLOAD_TASK_TAG = "download_task"

open class DownloadTask(uuid: String, createUserUUID: String, abstractFile: AbstractFile, val fileDataSource: FileDataSource, val fileDownloadParam: FileDownloadParam,
                        threadManager: ThreadManager) : Task(uuid, createUserUUID, abstractFile, threadManager) {

    protected lateinit var future: Future<Boolean>

    override fun getTypeResID(): Int {

        return R.drawable.download

    }

    override fun executeTask() {

        val abstractRemoteFile = abstractFile as AbstractRemoteFile

        val temporaryDownloadFile = abstractRemoteFile.getTemporaryDownloadFile(createUserUUID)

        val startingTaskState = StartingTaskState(0, abstractRemoteFile.size, "0KB/s", this)

        if (temporaryDownloadFile.exists()) {
            startingTaskState.setCurrentHandleFileSize(temporaryDownloadFile.length())
        } else
            startingTaskState.setCurrentHandleFileSize(0)

        setCurrentState(startingTaskState)

        val downloadFileCallable = OperateFileCallable(
                {
                    fileDataSource.downloadFile(fileDownloadParam, this)
                }
        )

        future = threadManager.runOnCacheThread(downloadFileCallable)

    }

    override fun doCancelTask() {
        super.doCancelTask()

        future.cancel(true)

    }

    override fun doDeleteTask() {
        super.doDeleteTask()

        val abstractRemoteFile = abstractFile as AbstractRemoteFile

        val temporaryDownloadFile = File(FileUtil.getDownloadFileStoreFolderPath() + abstractRemoteFile.parentFolderPath, uuid)

        val downloadFile = File(FileUtil.getDownloadFileStoreFolderPath() + abstractRemoteFile.parentFolderPath, abstractFile.name)

        if (temporaryDownloadFile.exists()) {

            val result = temporaryDownloadFile.delete()

            Log.d(DOWNLOAD_TASK_TAG, "delete task,so delete temporaryDownloadFile: $result")

        }

        if (downloadFile.exists()) {

            val result = downloadFile.delete()

            Log.d(DOWNLOAD_TASK_TAG, "delete task,so delete downloadFile: $result")

        }

    }

}

data class BTTaskParam(val transmission: Transmission)

class BTTask(uuid: String, createUserUUID: String, abstractFile: AbstractFile, threadManager: ThreadManager, val btTaskParam: BTTaskParam,
             val transmissionDataSource: TransmissionDataSource) : Task(uuid, createUserUUID, abstractFile, threadManager) {
    override fun getTypeResID(): Int {
        return R.drawable.bt_task
    }

    override fun executeTask() {

    }

    override fun doDeleteTask() {
        super.doDeleteTask()

        val transmission = btTaskParam.transmission

        transmissionDataSource.destroyTransmission(transmission.id, transmission.uuid, object : BaseOperateCallbackImpl() {})
    }

}

data class SMBTaskParam(val smbUrl: String)

class SMBTask(uuid: String, createUserUUID: String, abstractFile: AbstractFile, threadManager: ThreadManager, val smbTaskParam: SMBTaskParam)
    : Task(uuid, createUserUUID, abstractFile, threadManager) {
    override fun getTypeResID(): Int {

        return R.drawable.smb_task

    }

    override fun executeTask() {

    }

}


abstract class BaseMoveCopyTask(uuid: String, createUserUUID: String, srcFolder: AbstractFile,
                                threadManager: ThreadManager, val taskParam: TaskParam,
                                startSpeedHandler: Boolean = false) : Task(uuid, createUserUUID, srcFolder, threadManager, startSpeedHandler = startSpeedHandler) {

    private lateinit var refreshMoveCopyTaskHandler: RefreshMoveCopyTaskHandler


    fun startRefresh(transmissionTaskDataSource: TransmissionTaskDataSource) {
        refreshMoveCopyTaskHandler = RefreshMoveCopyTaskHandler(this, transmissionTaskDataSource)
        refreshMoveCopyTaskHandler.sendEmptyMessageDelayed(REFRESH_TASK, REFRESH_DELAY_TIME)
    }

    fun stopRefresh() {

        if (::refreshMoveCopyTaskHandler.isInitialized) {
            refreshMoveCopyTaskHandler.removeMessages(REFRESH_TASK)
        }

    }

}


data class TaskParam(val targetFolder: AbstractRemoteFile, val entries: List<AbstractRemoteFile>)

class MoveTask(uuid: String, createUserUUID: String, srcFolder: AbstractFile,
               threadManager: ThreadManager, taskParam: TaskParam,
               startSpeedHandler: Boolean = false) : BaseMoveCopyTask(uuid, createUserUUID, srcFolder, threadManager, taskParam, startSpeedHandler = startSpeedHandler) {

    override fun getTypeResID(): Int {

        return R.drawable.move_task
    }

    override fun executeTask() {

    }


}

class CopyTask(uuid: String, createUserUUID: String, srcFolder: AbstractFile,
               threadManager: ThreadManager, taskParam: TaskParam,
               startSpeedHandler: Boolean = false) : BaseMoveCopyTask(uuid, createUserUUID, srcFolder, threadManager, taskParam, startSpeedHandler = startSpeedHandler) {

    override fun getTypeResID(): Int {

        return R.drawable.copy_to
    }

    override fun executeTask() {

    }


}

private const val REFRESH_TASK = 0x1001
private const val REFRESH_DELAY_TIME = 2 * 1000L

private class RefreshMoveCopyTaskHandler(val task: Task, val transmissionTaskDataSource: TransmissionTaskDataSource) : Handler() {

    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)

        when (msg?.what) {

            REFRESH_TASK -> {

                if (task is BaseMoveCopyTask) {

                    Log.d(TASK_TAG, "start refresh transmission task")

                    refreshTaskState(task)

                }

            }

        }

    }

    private fun refreshTaskState(task: BaseMoveCopyTask) {
        transmissionTaskDataSource.getBaseMoveCopyTask(task.uuid, object : BaseOperateDataCallback<Task> {
            override fun onFail(operationResult: OperationResult?) {
                sendEmptyMessageDelayed(REFRESH_TASK, REFRESH_DELAY_TIME)
            }

            override fun onSucceed(data: Task, operationResult: OperationResult?) {

                val type = data.getCurrentState().getType()

                if (type == StateType.FINISH) {

                    if (task is CopyTask)
                        Log.d(TASK_TAG, "copy task finished,set current state and stop send message")
                    else if (task is MoveTask)
                        Log.d(TASK_TAG, "move task finished,set current state and stop send message")

                    task.setCurrentState(data.getCurrentState())

                } else if (type == StateType.ERROR) {

                    if (task is CopyTask)
                        Log.d(TASK_TAG, "copy task error,stop send message")
                    else if (task is MoveTask)
                        Log.d(TASK_TAG, "move task error,stop send message")

                } else {

                    if (task is CopyTask)
                        Log.d(TASK_TAG, "copy task not finished,send message")
                    else if (task is MoveTask)
                        Log.d(TASK_TAG, "move task not finished,send message")

                    sendEmptyMessageDelayed(REFRESH_TASK, REFRESH_DELAY_TIME)

                }

            }
        })
    }

}


