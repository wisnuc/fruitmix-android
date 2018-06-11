package com.winsun.fruitmix.newdesign201804.file.sharedFolder.data

import android.content.Context
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl

class InjectSharedFolderDataSource {

    companion object {

        fun inject(context: Context): SharedFolderDataSource {
            return SharedFolderRepository(ThreadManagerImpl.getInstance(), SharedFolderRemoteDataSource(InjectHttp.provideIHttpUtil(context),
                    InjectHttp.provideHttpRequestFactory(context)))
        }

    }

}