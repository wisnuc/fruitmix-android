package com.winsun.fruitmix.newdesign201804.file.transmission

import android.content.Context
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl

class InjectTransmissionDataSource {

    companion object {

        fun inject(context: Context): TransmissionDataSource {

            return TransmissionRepository(ThreadManagerImpl.getInstance(),
                    TransmissionRemoteDataSource(InjectHttp.provideIHttpUtil(context),
                            InjectHttp.provideHttpRequestFactory(context)))

        }

    }

}