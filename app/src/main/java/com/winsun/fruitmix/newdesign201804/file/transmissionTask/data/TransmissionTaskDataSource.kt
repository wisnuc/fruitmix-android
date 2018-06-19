package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task

interface TransmissionTaskDataSource {

    fun getAllTransmissionTasks(baseLoadDataCallback: BaseLoadDataCallback<Task>)

    fun getTransmissionTask(taskUUID:String,baseOperateDataCallback: BaseOperateDataCallback<Task>)

    fun getTransmissionTaskInCache(taskUUID: String):Task?

    fun addTransmissionTask(task: Task):Boolean

    fun deleteTransmissionTask(task: Task,baseOperateCallback: BaseOperateCallback)

}