package com.winsun.fruitmix.newdesign201804.file.list.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile

public interface FileDataSource{

    fun getFile(rootUUID:String,folderUUID:String,baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>)

    fun getFileByUUID(uuid:String):AbstractFile?

    fun searchFile(keys:List<String>,baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>)

}