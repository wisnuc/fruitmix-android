package com.winsun.fruitmix.parser

import android.database.Cursor
import com.winsun.fruitmix.db.DBHelper
import com.winsun.fruitmix.file.data.model.LocalFile
import com.winsun.fruitmix.file.data.model.LocalFolder
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.data.FileUploadParam
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.*
import com.winsun.fruitmix.thread.manage.ThreadManager

class LocalUploadTaskParser(val fileDataSource: FileDataSource,val threadManager: ThreadManager):LocalDataParser<UploadTask> {

    override fun parse(cursor: Cursor): UploadTask {

        val taskUUID = cursor.getString(cursor.getColumnIndex(DBHelper.TASK_UUID))
        val taskCreateUserUUID = cursor.getString(cursor.getColumnIndex(DBHelper.TASK_CREATE_USER_UUID))

        val taskStateTypeValue = cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_STATE))

        val taskFileSize =cursor.getLong(cursor.getColumnIndex(DBHelper.TASK_FILE_SIZE))
        val taskFileIsFolder = cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_FILE_IS_FOLDER)) == 1

        val taskFileName = cursor.getString(cursor.getColumnIndex(DBHelper.TASK_FILE_NAME))
        val taskFileTimestamp = cursor.getLong(cursor.getColumnIndex(DBHelper.TASK_FILE_TIMESTAMP))

        val taskRootUUID = cursor.getString(cursor.getColumnIndex(DBHelper.TASK_FILE_ROOT_UUID))
        val taskParentUUID = cursor.getString(cursor.getColumnIndex(DBHelper.TASK_FILE_PARENT_UUID))

        val abstractFile = if(taskFileIsFolder) LocalFolder() else LocalFile()

        abstractFile.name =taskFileName
        abstractFile.size = taskFileSize
        abstractFile.time = taskFileTimestamp
        abstractFile.path = cursor.getString(cursor.getColumnIndex(DBHelper.UPLOAD_TASK_FILE_LOCAL_PATH))

        val fileUploadParam = FileUploadParam(taskRootUUID,taskParentUUID,abstractFile)

        val uploadTask = UploadTask(taskUUID,taskCreateUserUUID,abstractFile,fileDataSource,fileUploadParam,threadManager)

        val taskState = when(taskStateTypeValue){
            StateType.FINISH.value -> FinishTaskState(taskFileSize,uploadTask)
            StateType.PAUSE.value -> PauseTaskState(0,taskFileSize,"0KB/s",uploadTask)
            else -> InitialTaskState(uploadTask)
        }

        uploadTask.setCurrentState(taskState)

        return uploadTask

    }

}