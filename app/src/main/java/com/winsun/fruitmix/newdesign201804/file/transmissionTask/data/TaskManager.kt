package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.DownloadTask
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.util.FileUtil

object TaskManager {

    private val tasks = mutableListOf<Task>()

    fun addTask(task: Task){

        renameFileNameIfNecessary(task)

        tasks.add(task)
    }

    fun deleteTask(task: Task):Boolean{
        return tasks.remove(task)
    }

    fun getAllTasks():List<Task>{
        return tasks
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

        task.abstractFile.name = newName

    }

}