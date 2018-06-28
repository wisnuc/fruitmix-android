package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

import android.util.Log
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
                         currentUserUUID: String, threadManager: ThreadManager)
    : DownloadTask(uuid, createUserUUID, abstractFile, fileDataSource, fileDownloadParam, currentUserUUID, threadManager) {

    private var currentDownloadedSize = 0L

    private var totalSize = 0L

    private val subDownloadTasks = mutableListOf<Task>()

    private lateinit var startingTaskState: StartingTaskState

    private val rootFolder = abstractFile as RemoteFolder

    override fun executeTask() {

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

        rootFolder.parentFolderPath = ""
        listFolder(rootFolder, { abstractRemoteFile, parentFolder -> analysisFolder(abstractRemoteFile, parentFolder) })

        restoreCurrentDownloadedSize()

        Log.d(TAG, "currentDownloadedSize:$currentDownloadedSize totalSize:$totalSize")

        startingTaskState = StartingTaskState(0, totalSize, "0KB/s", this)

        startingTaskState.setCurrentHandleFileSize(currentDownloadedSize)

        setCurrentState(startingTaskState)

        if (totalSize == 0L) {

            Log.d(TAG, "totalSize is 0,set current state to finish state")

            setCurrentState(FinishTaskState(totalSize, this))

        } else if (currentDownloadedSize == totalSize) {

            Log.d(TAG, "resume task,currentDownloadedSize == totalSize,set current state to finish state")

            setCurrentState(FinishTaskState(totalSize, this))

        } else
            listFolder(rootFolder, { abstractRemoteFile, parentFolder -> doDownloadFolder(abstractRemoteFile, parentFolder) })

    }

    private fun restoreCurrentDownloadedSize() {

        if (subDownloadTasks.isNotEmpty()) {

            subDownloadTasks.forEach {

                val downloadTask = it as DownloadTask

                if (downloadTask.getCurrentState().getType() == StateType.PAUSE) {

                    val temporaryFileSize = downloadTask.getTemporaryDownloadFile().length()

                    Log.d(TAG, "task temporary file size: $temporaryFileSize")

                    currentDownloadedSize += temporaryFileSize

                }


            }

        }

    }


    override fun setCurrentState(taskState: TaskState) {
        super.setCurrentState(taskState)

        if (taskState is PauseTaskState) {

            subDownloadTasks.forEach {
                it.pauseTask()
            }

        }

    }

    override fun deleteTask() {

        subDownloadTasks.forEach {

            it.deleteTask()

        }

        future.cancel(true)

        val result = FileTool.getInstance().deleteDir(
                FileUtil.getDownloadFileStoreFolderPath() + rootFolder.parentFolderPath + rootFolder.name)

        Log.d(TAG, "cancel download,delete folder result: $result")

    }

    private fun analysisFolder(abstractRemoteFile: AbstractRemoteFile, parentFolder: RemoteFolder) {

        if (abstractRemoteFile is RemoteFolder) {

            listFolder(abstractRemoteFile, { file, folder -> analysisFolder(file, folder) })

        } else {

            Log.d(TAG, "analysis file is not folder,size: " + abstractRemoteFile.size)

            val downloadFile = abstractRemoteFile.downloadedFile

            if (downloadFile.exists()) {

                currentDownloadedSize += abstractRemoteFile.size

                Log.d(TAG, "downloadFile exist,currentDownloadedSize: $currentDownloadedSize fileName:${abstractRemoteFile.name}")

                for (i in 0 until subDownloadTasks.size) {

                    val downloadTask = subDownloadTasks[i] as DownloadTask

                    if (downloadTask.abstractRemoteFile.uuid == abstractRemoteFile.uuid) {
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

            Log.d(TAG, "list folder size: " + abstractFiles.size)

            abstractFiles.forEach {

                it.parentFolderPath = parentFolder.name + File.separator

                handleFunc(it, parentFolder)

            }

        }

    }


    private fun doCreateFolder(abstractRemoteFile: AbstractRemoteFile) {

        val path = abstractRemoteFile.parentFolderPath + abstractRemoteFile.name

        val result = FileUtil.createFolderInDownloadFolder(path)

        Log.d(TAG, "createFolderInDownloadFolder: path: $path result: $result")

    }

    private var downloadFinishFileSize = 0L

    private fun doDownloadFolder(abstractRemoteFile: AbstractRemoteFile, parentFolder: RemoteFolder) {

        if (!abstractRemoteFile.isFolder) {

            var isTaskExist = false

            for (i in 0 until subDownloadTasks.size) {

                val downloadTask = subDownloadTasks[i] as DownloadTask

                if (downloadTask.abstractRemoteFile.uuid == abstractRemoteFile.uuid) {

                    Log.d(TAG, "task exist,resume,file name: " + abstractRemoteFile.name)

                    downloadTask.resumeTask()

                    isTaskExist = true
                    break
                }

            }

            if (isTaskExist)
                return

            Log.d(TAG, "create new task,download file name: " + abstractRemoteFile.name)

            val subTask = DownloadTask(Util.createLocalUUid(), createUserUUID, abstractRemoteFile, fileDataSource, abstractRemoteFile.createFileDownloadParam(),
                    currentUserUUID, threadManager)

            subDownloadTasks.add(subTask)

            subTask.registerObserver(object : TaskStateObserver {
                override fun notifyStateChanged(currentState: TaskState) {

                    if (currentState is StartingTaskState) {

                        startingTaskState.addCurrentHandleFileSize(currentState.speedSize)

                        setCurrentState(startingTaskState)

                    } else if (currentState is FinishTaskState) {

                        downloadFinishFileSize += abstractRemoteFile.size

                        if (downloadFinishFileSize == totalSize)
                            setCurrentState(FinishTaskState(totalSize, this@DownloadFolderTask))

                    } else if (currentState is ErrorTaskState)
                        setCurrentState(ErrorTaskState(this@DownloadFolderTask))

                }
            })

            subTask.init()

            subTask.startTask()

        }


    }


}