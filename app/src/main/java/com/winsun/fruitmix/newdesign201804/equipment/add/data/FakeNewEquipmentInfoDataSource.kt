package com.winsun.fruitmix.newdesign201804.equipment.add.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.equipment.search.data.Equipment
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.equipment.add.*
import com.winsun.fruitmix.user.User

class FakeNewEquipmentInfoDataSource : NewEquipmentInfoDataSource {

    override fun getAvailableEquipmentInfo(equipment: Equipment, baseLoadDataCallback: BaseLoadDataCallback<AvailableEquipmentInfo>) {

        val availableEquipmentDiskInfo = AvailableEquipmentDiskInfo(createTestUser(), 6 * 1024 * 1024 * 1024L, 15 * 1024 * 1024 * 1024L)

        val availableEquipmentInfo = AvailableEquipmentInfo(createTestEquipmentName(), availableEquipmentDiskInfo)

        baseLoadDataCallback.onSucceed(listOf(availableEquipmentInfo), OperationSuccess())

    }

    override fun getUnboundEquipmentInfo(equipment: Equipment, baseLoadDataCallback: BaseLoadDataCallback<UnBoundEquipmentInfo>) {

        val unboundEquipmentDiskInfo = UnboundEquipmentDiskInfo("TestOriginalEquipment",
                DiskMode.RAID1, 2 * 1024 * 1024 * 1024L, 12 * 1024 * 1024 * 1024L)

        val unBoundEquipmentInfo = UnBoundEquipmentInfo(createTestEquipmentName(), listOf(unboundEquipmentDiskInfo))

        baseLoadDataCallback.onSucceed(listOf(unBoundEquipmentInfo), OperationSuccess())

    }

    override fun getReinitializationEquipmentInfo(equipment: Equipment, baseLoadDataCallback: BaseLoadDataCallback<ReinitializationEquipmentInfo>) {

        val reinitializationEquipmentInfo = ReinitializationEquipmentInfo(createTestEquipmentName())

        baseLoadDataCallback.onSucceed(listOf(reinitializationEquipmentInfo), OperationSuccess())

    }

    private fun createTestUser(): User {

        val user = User()
        user.userName = "TestUser"

        return user

    }

    private fun createTestEquipmentName(): String {
        return "TestEquipment"
    }


}