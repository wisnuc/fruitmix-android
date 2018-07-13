package com.winsun.fruitmix.newdesign201804.file.list.data

import android.content.Context
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.newdesign201804.util.getCurrentUserUUID
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl

class InjectFileDataSource {

    companion object {
        fun inject(context: Context): FileDataSource {
            return FileDataRepository.getInstance(ThreadManagerImpl.getInstance(), InjectStationFileRepository.provideStationFileRepository(context),
                    injectFileNewOperateDataSource(context))
        }

        fun injectFileNewOperateDataSource(context: Context): FileNewOperateDataSource {

            return FileNewOperateDataSource(InjectHttp.provideHttpRequestFactory(context),
                    InjectHttp.provideIHttpUtil(context), InjectHttp.provideUploadFileInterface(),
                    ThreadManagerImpl.getInstance(), context.getCurrentUserUUID())

        }

    }

}