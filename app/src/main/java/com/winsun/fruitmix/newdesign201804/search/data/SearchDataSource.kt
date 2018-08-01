package com.winsun.fruitmix.newdesign201804.search.data

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile

enum class SearchOrder(val value:String){
    NEWEST("newest"),OLDEST("oldest"),FIND("find"),NUll("null")
}

interface SearchDataSource {

    fun searchFile(searchOrder:SearchOrder = SearchOrder.NUll, starti:String="", starte:String="",
                   last:String="", count:Int=100, places:List<String>, searchClasses:String="",
                   types:String="",tags:String="",name:String="",fileOnly:Boolean=false,baseLoadDataCallback: BaseLoadDataCallback<AbstractRemoteFile>)

}