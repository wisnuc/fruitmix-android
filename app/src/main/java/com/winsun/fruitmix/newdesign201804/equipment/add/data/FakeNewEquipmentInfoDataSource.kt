package com.winsun.fruitmix.newdesign201804.equipment.add.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.equipment.search.data.Equipment
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.user.User

class FakeNewEquipmentInfoDataSource : NewEquipmentInfoDataSource {

    private var equipmentCurrentCount = 0
    private var unboundDiskCount = 0

    override fun getAvailableEquipmentInfo(equipment: Equipment, baseLoadDataCallback: BaseLoadDataCallback<AvailableEquipmentInfo>) {

        val availableEquipmentDiskInfo = AvailableEquipmentDiskInfo(createTestUser(), 6.0 * 1024 * 1024 * 1024, 15.0 * 1024 * 1024 * 1024)

        val availableEquipmentInfo = AvailableEquipmentInfo(createTestEquipmentName(), equipment.hosts[0], availableEquipmentDiskInfo)

        baseLoadDataCallback.onSucceed(listOf(availableEquipmentInfo), OperationSuccess())

    }

    override fun getUnboundEquipmentInfo(equipment: Equipment, baseLoadDataCallback: BaseLoadDataCallback<UnBoundEquipmentInfo>) {

        val unboundDiskInfos = mutableListOf<UnboundEquipmentDiskInfo>()

        for (i in 0..unboundDiskCount) {

            val unboundEquipmentDiskInfo = UnboundEquipmentDiskInfo("TestOriginalEquipment$i",
                    DiskMode.RAID1, 2.0 * 1024 * 1024 * 1024, 12.0 * 1024 * 1024 * 1024)

            unboundDiskInfos.add(unboundEquipmentDiskInfo)

        }

        unboundDiskCount++

        val unBoundEquipmentInfo = UnBoundEquipmentInfo(createTestEquipmentName(), equipment.hosts[0], unboundDiskInfos)

        if (unboundDiskInfos.size == 1)
            unBoundEquipmentInfo.selectBoundEquipmentDiskInfo = unboundDiskInfos[0]

        baseLoadDataCallback.onSucceed(listOf(unBoundEquipmentInfo), OperationSuccess())

    }

    override fun getReinitializationEquipmentInfo(equipment: Equipment, baseLoadDataCallback: BaseLoadDataCallback<ReinitializationEquipmentInfo>) {

        val reinitializationEquipmentInfo = ReinitializationEquipmentInfo(createTestEquipmentName(), equipment.hosts[0])

        baseLoadDataCallback.onSucceed(listOf(reinitializationEquipmentInfo), OperationSuccess())

    }

    private fun createTestUser(): User {

        val user = User()
        user.userName = "TestUser"

        return user

    }

    private fun createTestEquipmentName(): String {
        val name = "TestEquipment$equipmentCurrentCount"

        equipmentCurrentCount++

        return name

    }


}