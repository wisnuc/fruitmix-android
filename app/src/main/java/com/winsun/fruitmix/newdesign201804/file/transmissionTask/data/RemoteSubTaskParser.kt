package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.SubTask
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.SubTaskError
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.SubTaskState
import org.json.JSONObject

class RemoteSubTaskParser {

    fun parse(jsonObject: JSONObject): SubTask {

        val parentUUID = jsonObject.optString("parent")

        val srcJSONObject = jsonObject.optJSONObject("src")

        val srcUUID = srcJSONObject.optString("uuid")
        val srcName = srcJSONObject.optString("name")

        val state = jsonObject.optString("state")

        var subTaskState = SubTaskState.WORKING

        if (state == "Conflict")
            subTaskState = SubTaskState.CONFLICT

        val errorJSONObject = jsonObject.optJSONObject("error")

        val code = errorJSONObject.optString("code")

        val subTaskError = SubTaskError(code)

        return SubTask(parentUUID, srcUUID, srcName, subTaskState, subTaskError)

    }

}