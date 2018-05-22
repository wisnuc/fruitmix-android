package com.winsun.fruitmix.newdesign201804.file.list.data

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.exception.NetworkException
import com.winsun.fruitmix.file.data.download.FileDownloadErrorState
import com.winsun.fruitmix.file.data.download.FileDownloadFinishedState
import com.winsun.fruitmix.file.data.download.FileDownloadState
import com.winsun.fruitmix.file.data.download.FileDownloadingState
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.http.*
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory
import com.winsun.fruitmix.model.operationResult.*
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.ErrorTaskState
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.util.FileUtil
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.MalformedURLException
import java.net.SocketTimeoutException

private const val OP = "op"
private const val RENAME = "rename"

private const val ROOT_DRIVE_PARAMETER = "/drives"
private const val DIRS = "/dirs/"

private const val TAG = "FileNewOperate"

private const val TASKS = "/tasks"

class FileNewOperateDataSource(httpRequestFactory: HttpRequestFactory, iHttpUtil: IHttpUtil)
    : BaseRemoteDataSourceImpl(iHttpUtil, httpRequestFactory) {

    fun renameFile(oldName: String, newName: String, driveUUID: String, dirUUID: String, callback: BaseOperateCallback) {

        val path = "$ROOT_DRIVE_PARAMETER/$driveUUID$DIRS$dirUUID/entries"

        val httpRequest = httpRequestFactory.createHttpPostRequest(path, "")

        if (!wrapper.checkPreCondition(httpRequest, callback)) {
            return
        }

        Log.d(TAG, "start rename")

        val httpResponse: HttpResponse?
        try {

            httpResponse = renameFileWithStationAPI(httpRequest, oldName, newName)

            if (httpResponse != null && httpResponse.responseCode == 200)
                callback.onSucceed()
            else
                callback.onFail(OperationNetworkException(httpResponse))

        } catch (e: MalformedURLException) {

            callback.onFail(OperationMalformedUrlException())

        } catch (e: SocketTimeoutException) {

            callback.onFail(OperationSocketTimeoutException())

        } catch (e: IOException) {

            e.printStackTrace()

            callback.onFail(OperationIOException())
        }

    }

    private fun renameFileWithStationAPI(httpRequest: HttpRequest, oldName: String, newName: String): HttpResponse? {

        val value: JSONObject
        try {
            value = JSONObject()

            value.put(OP, RENAME)

            val textFormData = TextFormData("$oldName|$newName", value.toString())

            return iHttpUtil.remoteCallRequest(iHttpUtil.createPostRequest(httpRequest,
                    iHttpUtil.createFormDataRequestBody(listOf(textFormData), emptyList())))

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return null

    }


    fun moveFile(srcFile: AbstractRemoteFile, targetFile: AbstractRemoteFile, entries: List<AbstractRemoteFile>, callback: BaseOperateCallback) {

        val value = generateTaskAPIBody("move", srcFile, targetFile, entries)

        val httpRequest = httpRequestFactory.createHttpPostRequest(TASKS, value.toString())

        if (!wrapper.checkPreCondition(httpRequest, callback)) {
            return
        }

        wrapper.operateCall(httpRequest, callback)

    }

    private fun generateTaskAPIBody(operateType: String, srcFile: AbstractRemoteFile, targetFile: AbstractRemoteFile, entries: List<AbstractRemoteFile>): JsonObject {
        val value = JsonObject()

        value.addProperty("type", operateType)

        val srcJsonObject = JsonObject()
        srcJsonObject.addProperty("drive", srcFile.rootFolderUUID)
        srcJsonObject.addProperty("dir", srcFile.parentFolderUUID)

        value.add("src", srcJsonObject)

        val targetJsonObject = JsonObject()
        targetJsonObject.addProperty("drive", targetFile.rootFolderUUID)
        targetJsonObject.addProperty("dir", targetFile.parentFolderUUID)

        value.add("dst", targetJsonObject)

        val entriesJsonArray = JsonArray()

        entries.forEach {
            entriesJsonArray.add(it.uuid)
        }

        value.add("entries", entriesJsonArray)

        Log.d(TAG, "task body: $value")

        return value
    }

    fun copyFile(srcFile: AbstractRemoteFile, targetFile: AbstractRemoteFile, entries: List<AbstractRemoteFile>, callback: BaseOperateCallback) {

        val value = generateTaskAPIBody("copy", srcFile, targetFile, entries)

        val httpRequest = httpRequestFactory.createHttpPostRequest(TASKS, value.toString())

        if (!wrapper.checkPreCondition(httpRequest, callback)) {
            return
        }

        wrapper.operateCall(httpRequest, callback)

    }

    fun downloadFile(fileDownloadParam: FileDownloadParam, task: Task, baseOperateCallback: BaseOperateCallback) {

        val httpRequest = httpRequestFactory.createHttpGetFileRequest(fileDownloadParam.fileDownloadPath)

        if (!wrapper.checkPreCondition(httpRequest, baseOperateCallback))
            return

        val responseBody: ResponseBody

        try {
            responseBody = iHttpUtil.getResponseBody(httpRequest)

            val result = FileUtil.writeResponseBodyToFolder(responseBody, task)

            Log.d(TAG, " download result:$result")

            if (result)
                baseOperateCallback.onSucceed()
            else
                baseOperateCallback.onFail(OperationIOException())

        } catch (e: NetworkException) {
            e.printStackTrace()

            task.setCurrentState(ErrorTaskState(task))

            baseOperateCallback.onFail(OperationNetworkException(e.httpResponse))

        }

    }


}