package com.winsun.fruitmix.newdesign201804.equipment.list.data

import android.content.Context

class InjectEquipmentItemDataSource {

    companion object {
        fun inject(context: Context): EquipmentItemDataSource {
            return FakeEquipmentItemDataSource
        }
    }

}
