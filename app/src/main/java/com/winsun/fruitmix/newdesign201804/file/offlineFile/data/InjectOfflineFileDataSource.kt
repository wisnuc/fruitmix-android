package com.winsun.fruitmix.newdesign201804.file.offlineFile.data

import android.content.Context

class InjectOfflineFileDataSource {

    companion object {
        fun inject(context: Context): OfflineFileDataSource {
            return FakeOfflineFileDataSource()
        }
    }


}