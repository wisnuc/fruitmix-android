package com.winsun.fruitmix.newdesign201804.file.sharedFolder.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl
import com.winsun.fruitmix.http.IHttpUtil
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory
import com.winsun.fruitmix.parser.RemoteRootDriveFolderParser
import com.winsun.fruitmix.user.User

private const val DRIVES = "/drives"

class SharedFolderRemoteDataSource(iHttpUtil: IHttpUtil, httpRequestFactory: HttpRequestFactory)
    : BaseRemoteDataSourceImpl(iHttpUtil, httpRequestFactory), SharedFolderDataSource {

    override fun createSharedDisk(sharedDiskName: String, users: List<User>, baseOperateDataCallback: BaseOperateDataCallback<AbstractRemoteFile>) {

        val rootJsonObject = JsonObject()

        val writeLists = JsonArray()

        users.forEach {
            writeLists.add(it.uuid)
        }

        rootJsonObject.add("writelist", writeLists)
        rootJsonObject.addProperty("label", sharedDiskName)

        val httpRequest = httpRequestFactory.createHttpPostRequest(DRIVES, rootJsonObject.toString())

        wrapper.operateCall(httpRequest, baseOperateDataCallback, RemoteRootDriveFolderParser())

    }

    override fun deleteSharedDisk(sharedDiskUUID: String, baseOperateCallback: BaseOperateCallback) {

        val httpPath = "$DRIVES/$sharedDiskUUID"

        val httpRequest = httpRequestFactory.createHttpDeleteRequest(httpPath, "")

        wrapper.operateCall(httpRequest, baseOperateCallback)

    }

}