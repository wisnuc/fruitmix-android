package com.winsun.fruitmix.newdesign201804.search.model

import com.google.gson.JsonArray
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.mediaModule.model.Media
import com.winsun.fruitmix.parser.BaseRemoteDataParser
import com.winsun.fruitmix.parser.RemoteDatasParser
import org.json.JSONArray

class RemoteSearchResultParser(val places: List<String>) : BaseRemoteDataParser(), RemoteDatasParser<AbstractRemoteFile> {

    override fun parse(json: String?): MutableList<AbstractRemoteFile> {

        val abstractRemoteFiles = mutableListOf<AbstractRemoteFile>()

        val root = checkHasWrapper(json)

        val rootJsonArray = JSONArray(root)

        for (i in 0 until rootJsonArray.length()) {

            val abstractRemoteFile: AbstractRemoteFile

            val jsonObject = rootJsonArray.getJSONObject(i)

            val uuid = jsonObject.optString("uuid")

            val parentUUID = jsonObject.optString("pdir")

            val name = jsonObject.optString("name")

            val size = jsonObject.optLong("size")

            val mTime = jsonObject.optLong("mtime")

            val placeNum = jsonObject.optInt("place")

            val hash = jsonObject.optString("hash")

            val type = jsonObject.optString("type")

            if (type == "directory") {
                abstractRemoteFile = RemoteFolder()
            } else {
                abstractRemoteFile = RemoteFile()

                abstractRemoteFile.fileHash = hash
            }

            abstractRemoteFile.uuid = uuid
            abstractRemoteFile.parentFolderUUID = parentUUID
            abstractRemoteFile.name = name
            abstractRemoteFile.size = size
            abstractRemoteFile.time = mTime

            //TODO:place must pass drive uuid,otherwise rootFolderUUID is wrong for call file api

            abstractRemoteFile.rootFolderUUID = places[placeNum]

            abstractRemoteFiles.add(abstractRemoteFile)

        }

        return abstractRemoteFiles
    }

}