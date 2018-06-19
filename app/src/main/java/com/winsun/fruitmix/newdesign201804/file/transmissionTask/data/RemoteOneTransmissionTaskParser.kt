package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.parser.BaseRemoteDataParser
import com.winsun.fruitmix.parser.RemoteDataParser
import com.winsun.fruitmix.thread.manage.ThreadManager
import org.json.JSONObject

class RemoteOneTransmissionTaskParser(val threadManager: ThreadManager,val currentUserUUID:String)
    :BaseRemoteDataParser(),RemoteDataParser<Task>{

    override fun parse(json: String?): Task {

        val root = checkHasWrapper(json)

        return RemoteTransmissionTaskJsonObjectParser(threadManager,currentUserUUID).parse(JSONObject(root))!!

    }

}