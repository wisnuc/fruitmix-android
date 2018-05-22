package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

import com.winsun.fruitmix.file.data.download.param.FileDownloadParam
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import java.util.concurrent.Callable

class DownloadFileCallable(val fileDataSource: FileDataSource, val fileDownloadParam: FileDownloadParam,
                           val task: Task, val currentUserUUID:String):Callable<Boolean> {


    override fun call(): Boolean {

        fileDataSource.downloadFile(fileDownloadParam,task)

        return true

    }

}