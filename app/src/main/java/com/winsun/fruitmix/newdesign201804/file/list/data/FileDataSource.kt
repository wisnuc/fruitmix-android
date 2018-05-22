package com.winsun.fruitmix.newdesign201804.file.list.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.http.HttpResponse
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.TaskState

interface FileDataSource{

    fun getFile(rootUUID:String,folderUUID:String,baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>)

    fun getFileByUUID(uuid:String):AbstractFile?

    fun searchFile(keys:List<String>,baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>)

    fun getRootDrive(baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>)

    fun createFolder(folderName: String, driveUUID: String, dirUUID: String, callback: BaseOperateDataCallback<HttpResponse>)

    fun renameFile(oldName:String,newName:String,driveUUID: String,dirUUID: String,callback: BaseOperateCallback)

    fun moveFile(srcFolder: AbstractRemoteFile, targetFolder:AbstractRemoteFile, entries:List<AbstractRemoteFile>, callback: BaseOperateCallback)

    fun copyFile(srcFolder: AbstractRemoteFile, targetFolder:AbstractRemoteFile, entries:List<AbstractRemoteFile>, callback: BaseOperateCallback)

    fun downloadFile(fileDownloadParam: FileDownloadParam, task: Task)

}