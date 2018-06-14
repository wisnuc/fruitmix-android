package com.winsun.fruitmix.newdesign201804.file.transmission

import com.winsun.fruitmix.BaseDataRepository
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.file.transmission.model.Transmission
import com.winsun.fruitmix.thread.manage.ThreadManager
import java.util.*

class TransmissionRepository(threadManager: ThreadManager,
                             private val transmissionDataSource: TransmissionDataSource) : BaseDataRepository(threadManager), TransmissionDataSource {


    override fun postMagnetTransmission(dirUUID: String, magnetUrl: String, baseOperateCallback: BaseOperateCallback) {

        mThreadManager.runOnCacheThread({
            transmissionDataSource.postMagnetTransmission(dirUUID, magnetUrl, baseOperateCallback)
        })

    }

    override fun getTransmission(baseLoadDataCallback: BaseLoadDataCallback<Transmission>) {
/*        mThreadManager.runOnCacheThread({
            transmissionDataSource.getTransmission(baseLoadDataCallback)
        })*/

        baseLoadDataCallback.onSucceed(Collections.emptyList(), OperationSuccess())

    }

    override fun pauseTransmission(id: String, baseOperateCallback: BaseOperateCallback) {
        mThreadManager.runOnCacheThread({
            transmissionDataSource.pauseTransmission(id, baseOperateCallback)
        })
    }

    override fun resumeTransmission(id: String, baseOperateCallback: BaseOperateCallback) {
        mThreadManager.runOnCacheThread({
            transmissionDataSource.resumeTransmission(id, baseOperateCallback)
        })
    }

    override fun destroyTransmission(id: String, uuid: String, baseOperateCallback: BaseOperateCallback) {
        mThreadManager.runOnCacheThread({
            transmissionDataSource.destroyTransmission(id, uuid, baseOperateCallback)
        })
    }

}