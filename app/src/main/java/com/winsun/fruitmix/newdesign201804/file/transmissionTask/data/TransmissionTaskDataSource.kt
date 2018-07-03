package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.ConflictSubTaskPolicy
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task

interface TransmissionTaskDataSource {

    fun getAllTransmissionTasks(baseLoadDataCallback: BaseLoadDataCallback<Task>)

    fun getBaseMoveCopyTask(taskUUID:String, baseOperateDataCallback: BaseOperateDataCallback<Task>)

    fun addTransmissionTask(task: Task):Boolean

    fun deleteTransmissionTask(task: Task,baseOperateCallback: BaseOperateCallback)

    fun updateUploadDownloadTaskState(task: Task,baseOperateCallback: BaseOperateCallback)

    fun updateConflictSubTask(taskUUID: String, nodeUUID:String, sameSourceConflictSubTaskPolicy: ConflictSubTaskPolicy,
                              diffSourceConflictSubTaskPolicy: ConflictSubTaskPolicy,
                              applyToAll:Boolean = false, baseOperateCallback: BaseOperateCallback)

}