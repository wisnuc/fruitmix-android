package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import android.content.Context

public class InjectTransmissionTaskDataSource {

    companion object {

        fun provideInstance(context: Context): TransmissionTaskDataSource {
            return TransmissionTaskRepository
        }

    }

}