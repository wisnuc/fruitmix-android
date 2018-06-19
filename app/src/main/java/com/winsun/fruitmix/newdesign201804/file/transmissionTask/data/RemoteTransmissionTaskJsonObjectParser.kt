package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.*
import com.winsun.fruitmix.thread.manage.ThreadManager
import org.json.JSONObject


class RemoteTransmissionTaskJsonObjectParser(val threadManager: ThreadManager,val currentUserUUID:String) {

    fun parse(jsonObject: JSONObject): Task? {

        val task: Task

        val uuid = jsonObject.optString("uuid")

        val type = jsonObject.optString("type")

        val src = jsonObject.optJSONObject("src")

        val srcRootUUID = src.optString("drive")
        val srcFolderUUID = src.optString("dir")

        val entries = jsonObject.optJSONArray("entries")

        val entryFiles = mutableListOf<AbstractRemoteFile>()

        for (j in 0 until entries.length()) {

            val fileName = entries.optString(j)

            val abstractRemoteFile = RemoteFile()
            abstractRemoteFile.name = fileName

            entryFiles.add(abstractRemoteFile)

        }

        val remoteFile = RemoteFile()
        remoteFile.name = entries.optString(0)
        remoteFile.rootFolderUUID = srcRootUUID
        remoteFile.parentFolderUUID = srcFolderUUID

        val dst = jsonObject.optJSONObject("dst")

        val targetRootUUID = dst.optString("drive")
        val targetFolderUUID = dst.optString("dir")

        val dstFile = RemoteFile()
        dstFile.rootFolderUUID = targetRootUUID
        dstFile.parentFolderUUID = targetFolderUUID

        val finished = jsonObject.optBoolean("finished")

        val taskParam = TaskParam(dstFile, entryFiles)

        if (type == "move") {

            task = MoveTask(uuid, currentUserUUID,remoteFile, threadManager, taskParam)

            task.init()

            if (finished)
                task.setCurrentState(FinishTaskState(0, task))
            else
                task.setCurrentState(StartingTaskState(0, 0, "", task))

            return task

        } else if (type == "copy") {

            task = CopyTask(uuid, currentUserUUID,remoteFile, threadManager, taskParam)

            task.init()

            if (finished)
                task.setCurrentState(FinishTaskState(0, task))
            else
                task.setCurrentState(StartingTaskState(0, 0, "", task))

            return task

        }

        return null

    }

}