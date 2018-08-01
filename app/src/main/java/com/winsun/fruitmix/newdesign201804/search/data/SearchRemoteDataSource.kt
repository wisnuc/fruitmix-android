package com.winsun.fruitmix.newdesign201804.search.data

import android.util.Log
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl
import com.winsun.fruitmix.http.IHttpUtil
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory
import com.winsun.fruitmix.newdesign201804.search.model.RemoteSearchResultParser

private const val TAG = "SearchRemoteDataSource"

class SearchRemoteDataSource(iHttpUtil: IHttpUtil, httpRequestFactory: HttpRequestFactory)
    : BaseRemoteDataSourceImpl(iHttpUtil, httpRequestFactory), SearchDataSource {

    override fun searchFile(searchOrder: SearchOrder, starti: String, starte: String, last: String, count: Int,
                            places: List<String>, searchClasses:String, types: String, tags: String,
                            name: String, fileOnly: Boolean, baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>) {

        val stringBuilder = StringBuilder("/files?")

        if(searchOrder != SearchOrder.NUll)
            stringBuilder.append("order=${searchOrder.value}&")

        if(starti.isNotEmpty())
            stringBuilder.append("starti=$starti&")

        if(starte.isNotEmpty())
            stringBuilder.append("starte=$starte&")

        if(last.isNotEmpty())
            stringBuilder.append("last=$last&")

        if(count > 0)
            stringBuilder.append("count=$count&")

        if(places.isNotEmpty()){

            val placeStringBuilder = StringBuilder()
            places.forEach {
                placeStringBuilder.append(it)
                placeStringBuilder.append(".")
            }

            val searchPlace = placeStringBuilder.substring(0,placeStringBuilder.lastIndex)

            stringBuilder.append("places=$searchPlace&")

        }

        if(searchClasses.isNotEmpty())
            stringBuilder.append("class=$searchClasses&")

        if(types.isNotEmpty())
            stringBuilder.append("types=$types&")

        if(tags.isNotEmpty())
            stringBuilder.append("tags=$tags&")

        if(name.isNotEmpty())
            stringBuilder.append("name=$name&")

        if(fileOnly)
            stringBuilder.append("fileOnly=true&")

        val path = stringBuilder.toString()

        Log.d(TAG, "search path: $path")

        val httpRequest = httpRequestFactory.createHttpGetRequest(path)

        wrapper.loadCall(httpRequest,baseLoadDataCallback,RemoteSearchResultParser(places))

    }

}