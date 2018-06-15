package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import android.content.Context
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl

public class InjectTransmissionTaskDataSource {

    companion object {

        fun provideInstance(context: Context): TransmissionTaskDataSource {

            val threadManager = ThreadManagerImpl.getInstance()

            return TransmissionTaskRepository(TaskManager, TransmissionTaskRemoteDataSource(threadManager,InjectHttp.provideIHttpUtil(context),
                    InjectHttp.provideHttpRequestFactory(context)), threadManager)
        }

    }

}