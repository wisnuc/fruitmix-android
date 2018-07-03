package com.winsun.fruitmix.newdesign201804.exitApp

import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateCallbackImpl
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.TransmissionTaskRepository

class ExitApp(val transmissionTaskRepository: TransmissionTaskRepository) {

    fun exitApp(){

        val tasks = transmissionTaskRepository.getAllDownloadUploadTransmissionTaskInCache()

        val baseOperateCallback = BaseOperateCallbackImpl()

        tasks.forEach {
            it.pauseTask()

            transmissionTaskRepository.updateUploadDownloadTaskState(it,baseOperateCallback)
        }

        transmissionTaskRepository.handleExitApp()
    }

}