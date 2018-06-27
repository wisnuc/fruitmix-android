package com.winsun.fruitmix.newdesign201804.file.list.data

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.exception.NetworkException
import com.winsun.fruitmix.file.data.download.FileDownloadErrorState
import com.winsun.fruitmix.file.data.download.FileDownloadFinishedState
import com.winsun.fruitmix.file.data.download.FileDownloadState
import com.winsun.fruitmix.file.data.download.FileDownloadingState
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.LocalFile
import com.winsun.fruitmix.file.data.model.LocalFolder
import com.winsun.fruitmix.http.*
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory
import com.winsun.fruitmix.model.operationResult.*
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.RemoteOneTransmissionTaskParser
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.TransmissionTaskDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.ErrorTaskState
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.StartingTaskState
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.newdesign201804.file.upload.UploadFileInterface
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.util.FileUtil
import com.winsun.fruitmix.util.Util
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.*
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.util.*

private const val OP = "op"
private const val RENAME = "rename"

private const val ROOT_DRIVE_PARAMETER = "/drives"
private const val DIRS = "/dirs/"

private const val TAG = "FileNewOperate"

private const val TASKS = "/tasks"

class FileNewOperateDataSource(httpRequestFactory: HttpRequestFactory, iHttpUtil: IHttpUtil,
                               val uploadFileInterface: UploadFileInterface,
                               val threadManager: ThreadManager, val currentUserUUID: String)
    : BaseRemoteDataSourceImpl(iHttpUtil, httpRequestFactory) {

    fun renameFile(oldName: String, newName: String, driveUUID: String, dirUUID: String, callback: BaseOperateCallback) {

        val path = "$ROOT_DRIVE_PARAMETER/$driveUUID$DIRS$dirUUID/entries"

        val httpRequest = httpRequestFactory.createHttpPostRequest(path, "")

        if (!wrapper.checkPreCondition(httpRequest, callback)) {
            return
        }


        Log.d(TAG, "start rename")

        val value = JsonObject()

        value.addProperty(OP, RENAME)

        val textFormData = TextFormData("$oldName|$newName", value.toString())

        wrapper.operateCall(httpRequest, Collections.singletonList(textFormData), Collections.emptyList(), callback)

    }

    fun moveFile(srcFile: AbstractRemoteFile, targetFile: AbstractRemoteFile, entries: List<AbstractRemoteFile>, callback: BaseOperateDataCallback<Task>) {

        val value = generateTaskAPIBody("move", srcFile, targetFile, entries)

        val httpRequest = httpRequestFactory.createHttpPostRequest(TASKS, value.toString())

        if (!wrapper.checkPreCondition(httpRequest, callback)) {
            return
        }

        wrapper.operateCall(httpRequest, callback, RemoteOneTransmissionTaskParser(threadManager, currentUserUUID))

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
            entriesJsonArray.add(it.name)
        }

        value.add("entries", entriesJsonArray)

        Log.d(TAG, "task body: $value")

        return value
    }

    fun copyFile(srcFile: AbstractRemoteFile, targetFile: AbstractRemoteFile, entries: List<AbstractRemoteFile>, callback: BaseOperateDataCallback<Task>) {

        val value = generateTaskAPIBody("copy", srcFile, targetFile, entries)

        val httpRequest = httpRequestFactory.createHttpPostRequest(TASKS, value.toString())

        if (!wrapper.checkPreCondition(httpRequest, callback)) {
            return
        }

        wrapper.operateCall(httpRequest, callback, RemoteOneTransmissionTaskParser(threadManager, currentUserUUID))

    }

    fun downloadFile(fileDownloadParam: FileDownloadParam, task: Task, baseOperateCallback: BaseOperateCallback) {

        val httpRequest = httpRequestFactory.createHttpGetFileRequest(fileDownloadParam.fileDownloadPath)

        if (!wrapper.checkPreCondition(httpRequest, baseOperateCallback))
            return

        val responseBody: ResponseBody


        val currentState = task.getCurrentState()

        val alreadyDownloadedFileSize = if (currentState is StartingTaskState)
            currentState.currentHandledSize
        else
            0

        if(alreadyDownloadedFileSize != 0L)
            httpRequest.addHeader("RANGE", "bytes=$alreadyDownloadedFileSize-")

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

    fun uploadFile(fileUploadParam: FileUploadParam, task: Task) {

        uploadFileWithProgress(fileUploadParam, task)
    }

    private fun uploadFileWithProgress(fileUploadParam: FileUploadParam, task: Task): OperationResult {

        val httpRequest = createUploadFileHttpRequest(fileUploadParam.driveUUID, fileUploadParam.dirUUID)

        if (!wrapper.checkUrl(httpRequest.url)) {
            return OperationMalformedUrlException()
        }

        Log.i(TAG, "uploadFile: start upload: " + httpRequest.url)

        val request: Request
        try {
            request = uploadFileInterface.createUploadFileWithProgressRequest(httpRequest,
                    getUploadFileRequestBody(httpRequest, fileUploadParam.abstractLocalFile as LocalFile), task)
        } catch (e: JSONException) {
            e.printStackTrace()

            return OperationJSONException()
        }

        return handleUploadFileRequest(request)

    }

    private fun handleUploadFileRequest(request: Request): OperationResult {
        val httpResponse: HttpResponse?
        try {

            httpResponse = iHttpUtil.remoteCallRequest(request)

            return if (httpResponse != null && httpResponse.responseCode == 200)
                OperationSuccess()
            else
                OperationNetworkException(httpResponse)

        } catch (e: MalformedURLException) {

            return OperationMalformedUrlException()

        } catch (ex: SocketTimeoutException) {

            return OperationSocketTimeoutException()

        } catch (e: IOException) {
            e.printStackTrace()

            return OperationIOException()
        }

    }


    private fun createUploadFileHttpRequest(driveUUID: String, dirUUID: String): HttpRequest {
        val path = "$ROOT_DRIVE_PARAMETER/$driveUUID$DIRS$dirUUID/entries"

        return httpRequestFactory.createHttpPostFileRequest(path, "")
    }

    @Throws(JSONException::class)
    private fun getUploadFileRequestBody(httpRequest: HttpRequest, localFile: LocalFile): RequestBody {
        val requestBody: RequestBody

        if (httpRequest.body.isNotEmpty()) {

            val jsonObject = JSONObject(httpRequest.body)

            jsonObject.put(OP, "newfile")
            jsonObject.put("toName", localFile.name)
            jsonObject.put(Util.SHA_256_STRING, localFile.fileHash)
            jsonObject.put(Util.SIZE_STRING, localFile.size)

            val jsonObjectStr = jsonObject.toString()

            Log.d(TAG, "uploadFile: $jsonObjectStr")

            val textFormData = TextFormData(Util.MANIFEST_STRING, jsonObjectStr)
            val fileFormData = FileFormData("", localFile.name, File(localFile.path))

            requestBody = iHttpUtil.createFormDataRequestBody(listOf(textFormData), listOf(fileFormData))

        } else {

            val jsonObject = JSONObject()

            jsonObject.put(OP, "newfile")
            jsonObject.put(Util.SIZE_STRING, localFile.size)
            jsonObject.put(Util.SHA_256_STRING, localFile.fileHash)

            val jsonObjectStr = jsonObject.toString()

            Log.d(TAG, "uploadFile: $jsonObjectStr")

            val fileFormData = FileFormData(localFile.name, jsonObjectStr, File(localFile.path))

            requestBody = iHttpUtil.createFormDataRequestBody(emptyList(), listOf(fileFormData))

        }
        return requestBody
    }


    fun deleteFile(fileName: String, driveUUID: String, dirUUID: String, callback: BaseOperateCallback) {

        val path = "$ROOT_DRIVE_PARAMETER/$driveUUID$DIRS$dirUUID/entries"

        val httpRequest = httpRequestFactory.createHttpPostRequest(path, "")

        if (!wrapper.checkPreCondition(httpRequest, callback)) {
            return
        }

        Log.d(TAG, "start delete")

        val value = JsonObject()

        value.addProperty(OP, "remove")

        val textFormData = TextFormData(fileName, value.toString())

        wrapper.operateCall(httpRequest, Collections.singletonList(textFormData), Collections.emptyList(), callback)

    }


}