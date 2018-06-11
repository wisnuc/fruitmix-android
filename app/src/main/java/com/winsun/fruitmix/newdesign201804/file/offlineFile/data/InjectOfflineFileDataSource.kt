package com.winsun.fruitmix.newdesign201804.file.offlineFile.data

import android.content.Context
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl

class InjectOfflineFileDataSource {

    companion object {

        fun inject(context: Context): OfflineFileDataSource {
            return OfflineFileRepository(ThreadManagerImpl.getInstance())
        }
    }

}