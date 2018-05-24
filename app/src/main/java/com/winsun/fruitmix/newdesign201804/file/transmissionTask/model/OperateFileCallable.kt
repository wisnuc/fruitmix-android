package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

import com.winsun.fruitmix.file.data.download.param.FileDownloadParam
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import java.util.concurrent.Callable

class OperateFileCallable(val operation: () -> Unit) : Callable<Boolean> {


    override fun call(): Boolean {

        operation()

        return true

    }

}