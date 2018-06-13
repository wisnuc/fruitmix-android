package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.DownloadTask
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.util.FileUtil

object TransmissionTaskRepository : TransmissionTaskDataSource {

    private val tasks = mutableListOf<Task>()

    override fun getAllTransmissionTasks(baseLoadDataCallback: BaseLoadDataCallback<Task>) {

        baseLoadDataCallback.onSucceed(tasks, OperationSuccess())

    }

    override fun addTransmissionTask(task: Task, baseOperateCallback: BaseOperateCallback) {

        renameFileNameIfNecessary(task)

        tasks.add(task)

        baseOperateCallback.onSucceed()
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

    override fun deleteTransmissionTask(task: Task, baseOperateCallback: BaseOperateCallback) {
        tasks.remove(task)

        baseOperateCallback.onSucceed()
    }

}