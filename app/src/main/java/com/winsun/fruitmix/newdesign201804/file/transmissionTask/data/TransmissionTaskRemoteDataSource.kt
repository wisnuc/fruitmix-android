package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl
import com.winsun.fruitmix.http.IHttpUtil
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.*
import com.winsun.fruitmix.thread.manage.ThreadManager

private const val TASK = "/tasks/"

class TransmissionTaskRemoteDataSource(val threadManager: ThreadManager, val currentUserUUID: String,
                                       iHttpUtil: IHttpUtil, httpRequestFactory: HttpRequestFactory)
    : BaseRemoteDataSourceImpl(iHttpUtil, httpRequestFactory), TransmissionTaskDataSource {

    override fun getAllTransmissionTasks(baseLoadDataCallback: BaseLoadDataCallback<Task>) {

        val httpRequest = httpRequestFactory.createHttpGetRequest(TASK)

        wrapper.loadCall(httpRequest, baseLoadDataCallback, RemoteTransmissionTasksParser(threadManager, currentUserUUID))

    }

    override fun getTransmissionTask(taskUUID: String, baseOperateDataCallback: BaseOperateDataCallback<Task>) {

        val httpRequest = httpRequestFactory.createHttpGetRequest("$TASK/$taskUUID")

        wrapper.operateCall(httpRequest, baseOperateDataCallback, RemoteOneTransmissionTaskParser(threadManager, currentUserUUID))

    }

    override fun getTransmissionTaskInCache(taskUUID: String): Task? {
        return null
    }

    override fun addTransmissionTask(task: Task): Boolean {
        return true
    }

    override fun deleteTransmissionTask(task: Task, baseOperateCallback: BaseOperateCallback) {

        var path = ""

        if (task is TransmissionTask) {
            path = TASK + task.uuid
        }

        if (path.isNotEmpty()) {
            val httpRequest = httpRequestFactory.createHttpDeleteRequest(path, "")

            wrapper.operateCall(httpRequest, baseOperateCallback)
        }

    }

    override fun updateConflictSubTask(taskUUID: String, nodeUUID: String, sameSourceConflictSubTaskPolicy: ConflictSubTaskPolicy,
                                       diffSourceConflictSubTaskPolicy: ConflictSubTaskPolicy,
                                       applyToAll: Boolean, baseOperateCallback: BaseOperateCallback) {

        val path = "$TASK$taskUUID/nodes/$nodeUUID"

        val body = JsonObject()

        val policyJsonArray = JsonArray()
        policyJsonArray.add(sameSourceConflictSubTaskPolicy.value)
        policyJsonArray.add(diffSourceConflictSubTaskPolicy.value)

        body.add("policy", policyJsonArray)
        body.addProperty("applyToAll", applyToAll)

        val httpRequest = httpRequestFactory.createHttpPatchRequest(path, body.toString())

        wrapper.operateCall(httpRequest, baseOperateCallback)

    }

}