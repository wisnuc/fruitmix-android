package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.*
import com.winsun.fruitmix.parser.BaseRemoteDataParser
import com.winsun.fruitmix.parser.RemoteDatasParser
import com.winsun.fruitmix.thread.manage.ThreadManager
import org.json.JSONArray

class RemoteTransmissionTaskParser(val threadManager: ThreadManager) : BaseRemoteDataParser(), RemoteDatasParser<Task> {

    override fun parse(json: String?): MutableList<Task> {

        val tasks = mutableListOf<Task>()

        val root = checkHasWrapper(json)

        val rootJsonArray = JSONArray(root)

        for (i in 0 until rootJsonArray.length()) {

            val task: Task

            val jsonObject = rootJsonArray.optJSONObject(i)

            val uuid = jsonObject.optString("uuid")

            val type = jsonObject.optString("type")

            val entries = jsonObject.optJSONArray("entries")

            val remoteFile = RemoteFile()
            remoteFile.name = entries.optString(0)

            val dst = jsonObject.optJSONObject("dst")

            val targetFolderUUID = dst.optString("dir")

            val finished = jsonObject.optBoolean("finished")

            if (type == "move") {
                task = MoveTask(uuid, remoteFile, threadManager, MoveTaskParam(targetFolderUUID))

                task.init()

                if (finished)
                    task.setCurrentState(FinishTaskState(0, task))
                else
                    task.setCurrentState(StartingTaskState(0, 0, "", task))

                tasks.add(task)
            } else if (type == "copy") {
                task = CopyTask(uuid, remoteFile, threadManager, CopyTaskParam(targetFolderUUID))

                task.init()

                if (finished)
                    task.setCurrentState(FinishTaskState(0, task))
                else
                    task.setCurrentState(StartingTaskState(0, 0, "", task))

                tasks.add(task)
            }

        }

        return tasks

    }

}