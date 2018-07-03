package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

import com.winsun.fruitmix.file.data.model.AbstractFile

class DownloadUploadFolderTaskStateObserver(val abstractFile: AbstractFile,
                                            val startingTaskState: StartingTaskState,
                                            val task:Task,
                                            val handleTaskFinished:()->Unit):TaskStateObserver {

    override fun notifyStateChanged(currentState: TaskState,preState:TaskState) {

        if (currentState is StartingTaskState) {

            startingTaskState.addCurrentHandleFileSize(currentState.speedSize)

            task.setCurrentState(startingTaskState)

        } else if (currentState is FinishTaskState) {

            handleTaskFinished()

        } else if (currentState is ErrorTaskState){

            task.deleteTask()
            task.setCurrentState(ErrorTaskState(task))

        }

    }

}