package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.DownloadTask
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.util.FileUtil

object TaskManager {

    private val tasks = mutableListOf<Task>()

    private val taskMap = mutableMapOf<String, Task>()

    fun addTask(task: Task): Boolean {

        renameFileNameIfNecessary(task)

        val result = tasks.add(task)

        taskMap[task.uuid] = task

        return result

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

            if (FileUtil.checkFileExistInDownloadFolder(newName))
                newName = FileUtil.renameFileName(code++, newName)
            else
                break

        }

        val abstractFile = task.abstractFile.copySelf()

        abstractFile.name = newName

        task.abstractFile = abstractFile

    }

}