package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.*
import com.winsun.fruitmix.parser.BaseRemoteDataParser
import com.winsun.fruitmix.parser.RemoteDatasParser
import com.winsun.fruitmix.thread.manage.ThreadManager
import org.json.JSONArray

class RemoteTransmissionTasksParser(val threadManager: ThreadManager,val currentUserUUID:String) : BaseRemoteDataParser(), RemoteDatasParser<Task> {

    override fun parse(json: String?): MutableList<Task> {

        val tasks = mutableListOf<Task>()

        val root = checkHasWrapper(json)

        val rootJsonArray = JSONArray(root)

        val remoteTransmissionTaskJsonObjectParser = RemoteTransmissionTaskJsonObjectParser(threadManager,currentUserUUID)

        for (i in 0 until rootJsonArray.length()) {

            val jsonObject = rootJsonArray.optJSONObject(i)

            val task = remoteTransmissionTaskJsonObjectParser.parse(jsonObject)

            if (task != null)
                tasks.add(task)

        }

        return tasks

    }

}