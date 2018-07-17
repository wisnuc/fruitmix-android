package com.winsun.fruitmix.newdesign201804.equipment.add.data

import android.content.Context
import com.winsun.fruitmix.equipment.search.data.Equipment
import com.winsun.fruitmix.equipment.search.data.EquipmentFoundedListener
import com.winsun.fruitmix.equipment.search.data.EquipmentSearchManager

class FakeEquipmentSearchManger : EquipmentSearchManager {

    override fun startDiscovery(context: Context,equipmentFoundedListener: EquipmentFoundedListener?) {

        val equipments = mutableListOf<Equipment>()

        equipments.add(Equipment("", mutableListOf("10.10.9.10"), 3000))
        equipments.add(Equipment("", mutableListOf("10.10.9.11"), 3000))
        equipments.add(Equipment("", mutableListOf("10.10.9.12"), 3000))
        equipments.add(Equipment("", mutableListOf("10.10.9.13"), 3000))

        equipments.forEach {

            equipmentFoundedListener?.call(it)

        }

    }

    override fun stopDiscovery() {

    }

}