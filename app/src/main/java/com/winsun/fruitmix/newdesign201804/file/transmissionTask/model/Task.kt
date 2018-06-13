package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

import android.util.Log
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseOperateCallbackImpl
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.data.FileUploadParam
import com.winsun.fruitmix.newdesign201804.file.transmission.TransmissionDataSource
import com.winsun.fruitmix.newdesign201804.file.transmission.model.Transmission
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.util.FileUtil
import java.io.File
import java.util.concurrent.Future

abstract class Task(val abstractFile: AbstractFile, val threadManager: ThreadManager, val max: Int = 100) {

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

    fun deleteTask() {
        currentTaskState.cancel()
    }

    abstract fun executeTask()

    open fun cancelTask() {

        taskStateObservers.clear()

    }

    fun registerObserver(taskStateObserver: TaskStateObserver) {
        taskStateObservers.add(taskStateObserver)
    }

    fun unregisterObserver(taskStateObserver: TaskStateObserver) {
        taskStateObservers.remove(taskStateObserver)
    }

    @Synchronized
    fun setCurrentState(taskState: TaskState) {

        if (taskState.getType() != currentTaskState.getType()) {

            currentTaskState.onFinishState()

            taskState.onStartState()

        }

        currentTaskState = taskState

        threadManager.runOnMainThread {

            taskStateObservers.forEach {
                it.notifyStateChanged(currentTaskState)
            }

        }

    }

    @Synchronized
    fun getCurrentState(): TaskState {
        return currentTaskState
    }

}

interface TaskStateObserver {

    fun notifyStateChanged(currentState: TaskState)

}

class UploadTask(abstractFile: AbstractFile, val fileDataSource: FileDataSource,
                 val fileUploadParam: FileUploadParam,
                 threadManager: ThreadManager) : Task(abstractFile, threadManager) {

    private lateinit var future: Future<Boolean>

    override fun getTypeResID(): Int {
        return R.drawable.upload
    }

    override fun executeTask() {

        val uploadFileCallable = OperateFileCallable {
            fileDataSource.uploadFile(fileUploadParam, this)
        }

        future = threadManager.runOnCacheThread(uploadFileCallable)

    }

    override fun cancelTask() {

        super.cancelTask()

        future.cancel(true)


    }

}

private const val DOWNLOAD_TASK_TAG = "download_task"

open class DownloadTask(abstractFile: AbstractFile, val fileDataSource: FileDataSource, val fileDownloadParam: FileDownloadParam,
                        val currentUserUUID: String, threadManager: ThreadManager) : Task(abstractFile, threadManager) {

    private var future: Future<Boolean>? = null

    override fun getTypeResID(): Int {

        return R.drawable.download

    }

    override fun executeTask() {

        val downloadFileCallable = OperateFileCallable(
                {
                    fileDataSource.downloadFile(fileDownloadParam, this)
                }
        )

        future = threadManager.runOnCacheThread(downloadFileCallable)

    }

    override fun cancelTask() {

        super.cancelTask()

        if (future != null)
            future?.cancel(true)

        val downloadFile = File(FileUtil.getDownloadFileStoreFolderPath(), abstractFile.name)

        if (downloadFile.exists()) {

            val result = downloadFile.delete()

            Log.d(DOWNLOAD_TASK_TAG, "cancel download,delete file result: $result")

        }

    }

}

data class BTTaskParam(val transmission: Transmission)

class BTTask(abstractFile: AbstractFile, threadManager: ThreadManager, val btTaskParam: BTTaskParam,
             val transmissionDataSource: TransmissionDataSource) : Task(abstractFile, threadManager) {
    override fun getTypeResID(): Int {
        return R.drawable.bt_task
    }

    override fun executeTask() {

    }

    override fun cancelTask() {
        super.cancelTask()

        val transmission = btTaskParam.transmission

        transmissionDataSource.destroyTransmission(transmission.id, transmission.uuid, object : BaseOperateCallbackImpl() {})
    }

}

data class SMBTaskParam(val smbUrl: String)

class SMBTask(abstractFile: AbstractFile, threadManager: ThreadManager, val smbTaskParam: SMBTaskParam) : Task(abstractFile, threadManager) {
    override fun getTypeResID(): Int {

        return R.drawable.smb_task

    }

    override fun executeTask() {

    }

    override fun cancelTask() {
        super.cancelTask()
    }

}

data class MoveTaskParam(val targetFolderUUID: String)

class MoveTask(abstractFile: AbstractFile, threadManager: ThreadManager, val moveTaskParam: MoveTaskParam) : Task(abstractFile, threadManager) {
    override fun getTypeResID(): Int {

        return R.drawable.move_task
    }

    override fun executeTask() {

    }

    override fun cancelTask() {
        super.cancelTask()

    }

}

data class CopyTaskParam(val targetFolderUUID: String)

class CopyTask(abstractFile: AbstractFile, threadManager: ThreadManager, val copyTaskParam: CopyTaskParam) : Task(abstractFile, threadManager) {
    override fun getTypeResID(): Int {

        return R.drawable.copy_to
    }

    override fun executeTask() {

    }

    override fun cancelTask() {
        super.cancelTask()

    }

}

