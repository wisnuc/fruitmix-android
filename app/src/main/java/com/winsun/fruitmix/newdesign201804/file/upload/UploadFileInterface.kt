package com.winsun.fruitmix.newdesign201804.file.upload

import com.winsun.fruitmix.http.HttpRequest
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import okhttp3.Request
import okhttp3.RequestBody

interface UploadFileInterface {

    fun createUploadFileWithProgressRequest(httpRequest: HttpRequest, requestBody: RequestBody,task: Task): Request

}