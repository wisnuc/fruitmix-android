package com.winsun.fruitmix.newdesign201804.file.transmission

import com.google.gson.JsonObject
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl
import com.winsun.fruitmix.http.IHttpUtil
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory
import com.winsun.fruitmix.newdesign201804.file.transmission.model.Transmission

private const val TRANSMISSION = "/transmission"
private const val OP = "op"

class TransmissionRemoteDataSource(iHttpUtil: IHttpUtil, httpRequestFactory: HttpRequestFactory)
    : BaseRemoteDataSourceImpl(iHttpUtil, httpRequestFactory), TransmissionDataSource {


    override fun postMagnetTransmission(dirUUID: String, magnetUrl: String, baseOperateCallback: BaseOperateCallback) {

        val jsonObject = JsonObject()
        jsonObject.addProperty("dirUUID", dirUUID)
        jsonObject.addProperty("magnetURL", magnetUrl)

        val httpRequest = httpRequestFactory.createHttpPostRequest(TRANSMISSION, jsonObject.toString())

        wrapper.operateCall(httpRequest, baseOperateCallback)

    }

    override fun getTransmission(baseLoadDataCallback: BaseLoadDataCallback<Transmission>) {

        val httpRequest = httpRequestFactory.createHttpGetRequest(TRANSMISSION)

        wrapper.loadCall(httpRequest, baseLoadDataCallback, RemoteTransmissionParser())

    }

    override fun pauseTransmission(id: String, baseOperateCallback: BaseOperateCallback) {

        val jsonObject = JsonObject()
        jsonObject.addProperty(OP, "pause")

        val httpRequest = httpRequestFactory.createHttpPatchRequest("$TRANSMISSION/$id", jsonObject.toString())

        wrapper.operateCall(httpRequest, baseOperateCallback)

    }

    override fun resumeTransmission(id: String, baseOperateCallback: BaseOperateCallback) {

        val jsonObject = JsonObject()
        jsonObject.addProperty(OP, "resume")

        val httpRequest = httpRequestFactory.createHttpPatchRequest("$TRANSMISSION/$id", jsonObject.toString())

        wrapper.operateCall(httpRequest, baseOperateCallback)

    }

    override fun destroyTransmission(id: String, uuid: String, baseOperateCallback: BaseOperateCallback) {

        val jsonObject = JsonObject()
        jsonObject.addProperty(OP, "destroy")
        jsonObject.addProperty("uuid", uuid)

        val httpRequest = httpRequestFactory.createHttpPatchRequest("$TRANSMISSION/$id", jsonObject.toString())

        wrapper.operateCall(httpRequest, baseOperateCallback)

    }

}