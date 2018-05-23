package com.winsun.fruitmix.newdesign201804.equipment.add.data

import android.content.Context
import com.winsun.fruitmix.equipment.search.data.InjectEquipment

class InjectNewEquipmentInfoDataSource {

    companion object {

        fun inject(context: Context): NewEquipmentInfoDataSource {
            return NewEquipmentInfoRepository(InjectEquipment.provideEquipmentDataSource(context))
        }
    }

}