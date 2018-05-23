package com.winsun.fruitmix.newdesign201804.equipment.add.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.equipment.search.data.Equipment
import com.winsun.fruitmix.equipment.search.data.EquipmentDataSource
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.user.User

class NewEquipmentInfoRepository(val equipmentDataSource: EquipmentDataSource) : NewEquipmentInfoDataSource {

    override fun getAvailableEquipmentInfo(equipment: Equipment, baseLoadDataCallback: BaseLoadDataCallback<AvailableEquipmentInfo>) {

        equipmentDataSource.getUsersInEquipment(equipment, object : BaseLoadDataCallback<User> {
            override fun onFail(operationResult: OperationResult?) {
                baseLoadDataCallback.onFail(operationResult)
            }

            override fun onSucceed(data: MutableList<User>?, operationResult: OperationResult?) {

                val availableEquipmentDiskInfo = AvailableEquipmentDiskInfo(data!![0], 6.0 * 1024 * 1024 * 1024, 15.0 * 1024 * 1024 * 1024)

                val availableEquipmentInfo = AvailableEquipmentInfo(equipment.equipmentName, equipment.hosts[0], availableEquipmentDiskInfo)

                baseLoadDataCallback.onSucceed(listOf(availableEquipmentInfo), OperationSuccess())

            }
        })


    }

    override fun getUnboundEquipmentInfo(equipment: Equipment, baseLoadDataCallback: BaseLoadDataCallback<UnBoundEquipmentInfo>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getReinitializationEquipmentInfo(equipment: Equipment, baseLoadDataCallback: BaseLoadDataCallback<ReinitializationEquipmentInfo>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}