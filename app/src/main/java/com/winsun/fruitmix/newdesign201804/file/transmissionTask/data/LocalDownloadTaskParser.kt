package com.winsun.fruitmix.newdesign201804.file.transmissionTask.data

import android.database.Cursor
import com.winsun.fruitmix.db.DBHelper
import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.file.data.station.StationFileRepository
import com.winsun.fruitmix.newdesign201804.component.createFileDownloadParam
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.*
import com.winsun.fruitmix.parser.LocalDataParser
import com.winsun.fruitmix.thread.manage.ThreadManager

class LocalDownloadTaskParser(val fileDataSource: FileDataSource, val threadManager: ThreadManager,
                              val stationFileRepository: StationFileRepository) : LocalDataParser<DownloadTask> {

    override fun parse(cursor: Cursor): DownloadTask {

        val taskUUID = cursor.getString(cursor.getColumnIndex(DBHelper.TASK_UUID))
        val taskCreateUserUUID = cursor.getString(cursor.getColumnIndex(DBHelper.TASK_CREATE_USER_UUID))

        val taskStateTypeValue = cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_STATE))

        val taskFileSize = cursor.getLong(cursor.getColumnIndex(DBHelper.TASK_FILE_SIZE))
        val taskFileIsFolder = cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_FILE_IS_FOLDER)) == 1

        val taskFileName = cursor.getString(cursor.getColumnIndex(DBHelper.TASK_FILE_NAME))
        val taskFileTimestamp = cursor.getLong(cursor.getColumnIndex(DBHelper.TASK_FILE_TIMESTAMP))

        val taskRootUUID = cursor.getString(cursor.getColumnIndex(DBHelper.TASK_FILE_ROOT_UUID))
        val taskParentUUID = cursor.getString(cursor.getColumnIndex(DBHelper.TASK_FILE_PARENT_UUID))

        val abstractFile = if (taskFileIsFolder) RemoteFolder() else RemoteFile()

        abstractFile.rootFolderUUID = taskRootUUID
        abstractFile.parentFolderUUID = taskParentUUID
        abstractFile.name = taskFileName
        abstractFile.size = taskFileSize
        abstractFile.time = taskFileTimestamp
        abstractFile.uuid = cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOAD_TASK_FILE_REMOTE_UUID))

        val fileDownloadParam = abstractFile.createFileDownloadParam()

        val downloadTask =
                if (taskFileIsFolder)
                    DownloadFolderTask(stationFileRepository, taskUUID, taskCreateUserUUID, abstractFile, fileDataSource,
                            fileDownloadParam, threadManager)
                else
                    DownloadTask(taskUUID, taskCreateUserUUID, abstractFile, fileDataSource, fileDownloadParam, threadManager)

        downloadTask.init()

        val taskState = when (taskStateTypeValue) {
            StateType.FINISH.value -> FinishTaskState(taskFileSize, downloadTask)
            StateType.PAUSE.value -> PauseTaskState(0, taskFileSize, "0KB/s", downloadTask)
            else -> InitialTaskState(downloadTask)
        }

        downloadTask.setCurrentState(taskState)

        return downloadTask

    }

}