package com.winsun.fruitmix.newdesign201804.file.offlineFile.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.file.data.model.AbstractLocalFile

interface OfflineFileDataSource {

    fun getFile(baseLoadDataCallback: BaseLoadDataCallback<AbstractLocalFile>)

}