package com.winsun.fruitmix.newdesign201804.file.list.data

import android.util.Log
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.http.*
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory
import com.winsun.fruitmix.model.operationResult.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.MalformedURLException
import java.net.SocketTimeoutException

private const val OP = "op"
private const val RENAME = "rename"

private const val ROOT_DRIVE_PARAMETER = "/drives"
private const val DIRS = "/dirs/"

private const val TAG = "FileNewOperate"

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

}