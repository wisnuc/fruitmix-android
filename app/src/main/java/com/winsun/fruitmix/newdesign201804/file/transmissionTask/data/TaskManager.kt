package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.callback.BaseOperateCallbackImpl
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.*
import com.winsun.fruitmix.util.FileUtil

object TaskManager : TaskStateObserver {

    private var isCacheDirty = true

    private val tasks = mutableListOf<Task>()

    private val taskMap = mutableMapOf<String, Task>()

    private var transmissionTaskDBDataSource: TransmissionTaskDBDataSource? = null

    private val baseOperateCallback = BaseOperateCallbackImpl()

    fun init(transmissionTaskDBDataSource: TransmissionTaskDBDataSource) {
        this.transmissionTaskDBDataSource = transmissionTaskDBDataSource
    }

    fun isCacheDirty(): Boolean {
        return isCacheDirty
    }

    fun setCacheDirty(isCacheDirty: Boolean) {
        this.isCacheDirty = isCacheDirty
    }

    fun addTask(task: Task): Boolean {

        renameFileNameIfNecessary(task)

        val result = tasks.add(task)

        taskMap[task.uuid] = task

        if (transmissionTaskDBDataSource != null) {
            task.registerObserver(this)
        }

        return result

    }

    override fun notifyStateChanged(currentState: TaskState, preState: TaskState) {

        val currentStateType = currentState.getType()

        if (currentStateType != preState.getType() &&
                (currentStateType == StateType.PAUSE || currentStateType == StateType.FINISH || currentStateType == StateType.ERROR))
            transmissionTaskDBDataSource?.updateUploadDownloadTaskState(currentState.task, baseOperateCallback)

    }

    fun handleExitApp() {
        tasks.forEach {
            it.unregisterObserver(this)
        }
    }

    fun deleteTask(task: Task): Boolean {
        return tasks.remove(task)
    }

    fun getAllTasks(): List<Task> {
        return tasks
    }

    fun checkTaskExist(task: Task): Boolean {

        return taskMap.containsKey(task.uuid)

    }

    fun getTask(taskUUID: String): Task? {
        return taskMap[taskUUID]
    }

    private fun renameFileNameIfNecessary(task: Task) {
        var code = 1

        val originalName = task.abstractFile.name
        var newName = originalName

        while (true) {

            val list = tasks.filter { it is DownloadTask }

            val result = list.filter { it.abstractFile.name == task.abstractFile.name }

            if (result.size > 1) {
                newName = FileUtil.renameFileName(code++, newName)
            }

            if (task is DownloadTask && FileUtil.checkFileExistInDownloadFolder(newName, task.createUserUUID))
                newName = FileUtil.renameFileName(code++, newName)
            else
                break

        }

        val abstractFile = task.abstractFile.copySelf()

        abstractFile.name = newName

        task.abstractFile = abstractFile

    }

}