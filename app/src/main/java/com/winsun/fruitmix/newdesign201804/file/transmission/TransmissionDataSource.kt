package com.winsun.fruitmix.newdesign201804.file.transmission

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.newdesign201804.file.transmission.model.Transmission

interface TransmissionDataSource {

    fun postMagnetTransmission(dirUUID: String, magnetUrl: String, baseOperateCallback: BaseOperateCallback)

    fun getTransmission(baseLoadDataCallback: BaseLoadDataCallback<Transmission>)

    fun pauseTransmission(id: String, baseOperateCallback: BaseOperateCallback)

    fun resumeTransmission(id: String, baseOperateCallback: BaseOperateCallback)

    fun destroyTransmission(id: String, uuid: String, baseOperateCallback: BaseOperateCallback)

}