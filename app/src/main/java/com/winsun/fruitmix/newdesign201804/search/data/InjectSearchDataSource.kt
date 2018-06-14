package com.winsun.fruitmix.newdesign201804.search.data

import android.content.Context
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl

class InjectSearchDataSource {

    companion object {

        fun inject(context: Context):SearchDataSource{
            return SearchDataRepository(ThreadManagerImpl.getInstance(),
                    SearchRemoteDataSource(InjectHttp.provideIHttpUtil(context),InjectHttp.provideHttpRequestFactory(context)))
        }

    }

}