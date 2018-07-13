package com.winsun.fruitmix.newdesign201804.search.data

import com.winsun.fruitmix.model.BaseDataRepository
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.thread.manage.ThreadManager

class SearchDataRepository(threadManager: ThreadManager, val searchDataSource: SearchDataSource) : BaseDataRepository(threadManager), SearchDataSource {

    override fun searchFile(searchOrder: SearchOrder, starti: String, starte: String, last: String,
                            count: Int, places: String, searchClasses: String, types: String,
                            tags: String, name: String, fileOnly: Boolean, baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>) {

        mThreadManager.runOnCacheThread({
            searchDataSource.searchFile(searchOrder, starti, starte, last, count, places, searchClasses, types,
                    tags, name, fileOnly, createLoadCallbackRunOnMainThread(baseLoadDataCallback))
        })

    }


}