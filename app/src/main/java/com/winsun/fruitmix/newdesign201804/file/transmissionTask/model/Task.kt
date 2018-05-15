package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

import com.winsun.fruitmix.R
import com.winsun.fruitmix.file.data.model.AbstractFile

abstract class Task(val abstractFile: AbstractFile) {

    abstract fun getTypeResID(): Int

    private lateinit var currentTaskState: TaskState

    private val taskStateObservers = mutableListOf<TaskStateObserver>()

    //after construct task,call init(),
    fun init() {

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

    abstract fun cancelTask()

    fun registerObserver(taskStateObserver: TaskStateObserver) {
        taskStateObservers.add(taskStateObserver)
    }

    fun unregisterObserver(taskStateObserver: TaskStateObserver) {
        taskStateObservers.remove(taskStateObserver)
    }

    fun setCurrentState(taskState: TaskState) {

        currentTaskState = taskState

        taskStateObservers.forEach {
            it.notifyStateChanged(currentTaskState)
        }

    }

}

interface TaskStateObserver {

    fun notifyStateChanged(currentState: TaskState)

}

data class UploadTaskParam(val updateFolderUUID: String)

class UploadTask(abstractFile: AbstractFile, val updateTaskParam: UploadTaskParam) : Task(abstractFile) {
    override fun getTypeResID(): Int {
        return R.drawable.upload
    }

    override fun executeTask() {

    }

    override fun cancelTask() {

    }

}

data class DownloadTaskParam(val downloadFolderUUID: String)

class DownloadTask(abstractFile: AbstractFile, val downloadTaskParam: DownloadTaskParam) : Task(abstractFile) {
    override fun getTypeResID(): Int {

        return R.drawable.download
    }

    override fun executeTask() {

    }

    override fun cancelTask() {

    }

}

data class BTTaskParam(val btUrl: String)

class BTTask(abstractFile: AbstractFile, val btTaskParam: BTTaskParam) : Task(abstractFile) {
    override fun getTypeResID(): Int {
        return R.drawable.bt_task
    }

    override fun executeTask() {

    }

    override fun cancelTask() {

    }

}

data class SMBTaskParam(val smbUrl: String)

class SMBTask(abstractFile: AbstractFile, val smbTaskParam: SMBTaskParam) : Task(abstractFile) {
    override fun getTypeResID(): Int {

        return R.drawable.smb_task
    }

    override fun executeTask() {

    }

    override fun cancelTask() {

    }

}

data class MoveTaskParam(val targetFolderUUID: String)

class MoveTask(abstractFile: AbstractFile, val moveTaskParam: MoveTaskParam) : Task(abstractFile) {
    override fun getTypeResID(): Int {

        return R.drawable.move_task
    }

    override fun executeTask() {

    }

    override fun cancelTask() {

    }

}

data class CopyTaskParam(val targetFolderUUID: String)

class CopyTask(abstractFile: AbstractFile, val copyTaskParam: CopyTaskParam) : Task(abstractFile) {
    override fun getTypeResID(): Int {

        return R.drawable.copy_to
    }

    override fun executeTask() {

    }

    override fun cancelTask() {

    }

}

