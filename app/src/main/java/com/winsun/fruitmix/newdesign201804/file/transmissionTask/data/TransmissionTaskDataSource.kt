package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task

interface TransmissionTaskDataSource {

    fun getAllTransmissionTasks(baseLoadDataCallback: BaseLoadDataCallback<Task>)

    fun addTransmissionTask(task: Task,baseOperateCallback: BaseOperateCallback)

}