package com.winsun.fruitmix.newdesign201804.file.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile

public interface FileDataSource{

    fun getFile(baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>)

}