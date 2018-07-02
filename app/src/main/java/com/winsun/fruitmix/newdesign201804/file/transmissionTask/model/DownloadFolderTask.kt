package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

import android.util.Log
import android.util.Log.d
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.file.data.station.StationFileRepository
import com.winsun.fruitmix.model.operationResult.OperationSuccessWithFile
import com.winsun.fruitmix.newdesign201804.component.createFileDownloadParam
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.util.FileTool
import com.winsun.fruitmix.util.FileUtil
import com.winsun.fruitmix.util.Util
import java.io.File

private const val TAG = "DownloadFolderTask"

class DownloadFolderTask(val stationFileRepository: StationFileRepository,
                         uuid: String, createUserUUID: String,
                         abstractFile: AbstractFile, fileDataSource: FileDataSource, fileDownloadParam: FileDownloadParam,
                         threadManager: ThreadManager)
    : DownloadTask(uuid, createUserUUID, abstractFile, fileDataSource, fileDownloadParam, threadManager) {

    private var currentDownloadedSize = 0L

    private var totalSize = 0L

    private var downloadFinishFileSize = 0L

    private val subDownloadTaskMaps = mutableMapOf<TaskStateObserver, Task>()

    private lateinit var startingTaskState: StartingTaskState

    override fun executeTask() {

        val rootFolder = abstractFile as RemoteFolder

        val downloadFolderCallable = OperateFileCallable(
                {
                    downloadFolder(rootFolder)
                }
        )

        future = threadManager.runOnCacheThread(downloadFolderCallable)

    }

    private fun downloadFolder(rootFolder: RemoteFolder) {

        currentDownloadedSize = 0L
        totalSize = 0L
        downloadFinishFileSize = 0L

        rootFolder.parentFolderPath = ""
        listFolder(rootFolder, { abstractRemoteFile, parentFolder -> analysisFolder(abstractRemoteFile, parentFolder) })

        restoreCurrentDownloadedSize()

        d(TAG, "currentDownloadedSize:$currentDownloadedSize totalSize:$totalSize taskUUID: $uuid")

        startingTaskState = StartingTaskState(0, totalSize, "0KB/s", this)

        startingTaskState.setCurrentHandleFileSize(currentDownloadedSize)

        setCurrentState(startingTaskState)

        if (totalSize == 0L) {

            d(TAG, "totalSize is 0,set current state to finish state")

            setCurrentState(FinishTaskState(totalSize, this))

        } else if (currentDownloadedSize == totalSize) {

            d(TAG, "resume task,currentDownloadedSize == totalSize,set current state to finish state")

            setCurrentState(FinishTaskState(totalSize, this))

        } else
            listFolder(rootFolder, { abstractRemoteFile, parentFolder -> doDownloadFolder(abstractRemoteFile, parentFolder) })

    }

    private fun restoreCurrentDownloadedSize() {

        if (subDownloadTaskMaps.isNotEmpty()) {

            subDownloadTaskMaps.forEach {

                val downloadTask = it as DownloadTask

                if (downloadTask.getCurrentState().getType() == StateType.PAUSE) {

                    val temporaryFile = downloadTask.abstractFile as AbstractRemoteFile

                    val temporaryFileSize = temporaryFile.getDownloadedFile(createUserUUID).length()

                    d(TAG, "task temporary file size: $temporaryFileSize")

                    currentDownloadedSize += temporaryFileSize

                }

            }

        }

    }


    override fun setCurrentState(taskState: TaskState) {
        super.setCurrentState(taskState)

        if (taskState is PauseTaskState) {

            subDownloadTaskMaps.forEach {

                it.value.pauseTask()

            }

        }

    }

    override fun doDeleteTask() {

        subDownloadTaskMaps.forEach {

            it.value.unregisterObserver(it.key)
            it.value.deleteTask()

        }

        val rootFolder = abstractFile as RemoteFolder

        val result = FileTool.getInstance().deleteDir(
                FileUtil.getDownloadFileStoreFolderPath() + rootFolder.parentFolderPath + rootFolder.name)

        d(TAG, "cancel download,delete folder result: $result")

    }

    private fun analysisFolder(abstractRemoteFile: AbstractRemoteFile, parentFolder: RemoteFolder) {

        if (abstractRemoteFile is RemoteFolder) {

            listFolder(abstractRemoteFile, { file, folder -> analysisFolder(file, folder) })

        } else {

            d(TAG, "analysis file is not folder,size: " + abstractRemoteFile.size + " name: " + abstractRemoteFile.name)

            val downloadFile = abstractRemoteFile.getDownloadedFile(createUserUUID)

            if (downloadFile.exists()) {

                currentDownloadedSize += abstractRemoteFile.size

                Log.d(TAG, "downloadFile exist,currentDownloadedSize: $currentDownloadedSize fileName:${abstractRemoteFile.name}")

                val subTasks = subDownloadTaskMaps.values

                val iterator = subTasks.iterator()

                while (iterator.hasNext()) {

                    val downloadTask = iterator.next() as DownloadTask

                    val remoteFile = downloadTask.abstractFile as AbstractRemoteFile

                    if (remoteFile.uuid == abstractRemoteFile.uuid) {
                        downloadFinishFileSize += abstractRemoteFile.size
                        downloadTask.setCurrentState(FinishTaskState(abstractRemoteFile.size, downloadTask))
                    }

                }

            }

            totalSize += abstractRemoteFile.size

        }

    }


    private fun listFolder(parentFolder: RemoteFolder, handleFunc: (abstractRemoteFile: AbstractRemoteFile, parentFolder: RemoteFolder) -> Unit) {

        val operationResult = stationFileRepository.getFileWithoutCreateNewThread(parentFolder.rootFolderUUID, parentFolder.uuid,
                parentFolder.name)

        if (operationResult is OperationSuccessWithFile) {

            doCreateFolder(parentFolder)

            val abstractFiles = operationResult.list

            d(TAG, "list folder size: " + abstractFiles.size)

            abstractFiles.forEach {

                it.parentFolderPath = parentFolder.name + File.separator

                handleFunc(it, parentFolder)

            }

        }

    }


    private fun doCreateFolder(abstractRemoteFile: AbstractRemoteFile) {

        val path = abstractRemoteFile.parentFolderPath + abstractRemoteFile.name

        val result = FileUtil.createFolderInDownloadFolder(path)

        d(TAG, "createFolderInDownloadFolder: path: $path result: $result")

    }

    private fun doDownloadFolder(abstractRemoteFile: AbstractRemoteFile, parentFolder: RemoteFolder) {

        if (!abstractRemoteFile.isFolder) {

            var isTaskExist = false

            val subTasks = subDownloadTaskMaps.values

            val iterator = subTasks.iterator()

            while (iterator.hasNext()) {

                val downloadTask = iterator.next() as DownloadTask

                val remoteFile = downloadTask.abstractFile as AbstractRemoteFile

                if (remoteFile.uuid == abstractRemoteFile.uuid) {

                    d(TAG, "task exist,resume,file name: " + abstractRemoteFile.name)

                    downloadTask.resumeTask()

                    isTaskExist = true
                    break
                }

            }

            if (isTaskExist)
                return

            d(TAG, "create new task,download file name: " + abstractRemoteFile.name)

            val subTask = DownloadTask(Util.createLocalUUid(), createUserUUID, abstractRemoteFile,
                    fileDataSource, abstractRemoteFile.createFileDownloadParam(), threadManager)

            val taskStateObserver = object : TaskStateObserver {
                override fun notifyStateChanged(currentState: TaskState) {

                    if (currentState is StartingTaskState) {

                        d(TAG, "addCurrentHandleFileSize: " + currentState.speedSize + " fileName: " + abstractRemoteFile.name)

                        startingTaskState.addCurrentHandleFileSize(currentState.speedSize)

                        setCurrentState(startingTaskState)

                    } else if (currentState is FinishTaskState) {

                        downloadFinishFileSize += abstractRemoteFile.size

                        Log.d(TAG, "file: " + abstractRemoteFile.name + " finish download," +
                                "downloadFinishFileSize: " + downloadFinishFileSize + " totalSize: " + totalSize)

                        if (downloadFinishFileSize == totalSize)
                            setCurrentState(FinishTaskState(totalSize, this@DownloadFolderTask))

                        subTask.unregisterObserver(this)

                    } else if (currentState is ErrorTaskState) {

                        Log.d(TAG, "file: " + abstractRemoteFile.name + " error occur when download")

                        setCurrentState(ErrorTaskState(this@DownloadFolderTask))

                    }

                }
            }

            subTask.registerObserver(taskStateObserver)

            subDownloadTaskMaps[taskStateObserver] = subTask

            subTask.init()
            subTask.startTask()

        }


    }


}