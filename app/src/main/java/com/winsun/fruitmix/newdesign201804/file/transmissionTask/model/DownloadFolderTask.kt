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

        rootFolder.parentFolderPath = ""
        listFolder(rootFolder, { abstractRemoteFile, parentFolder -> analysisFolder(abstractRemoteFile, parentFolder) })

        startingTaskState = StartingTaskState(0, totalSize, "0KB/s", this)

        setCurrentState(startingTaskState)

        if (totalSize == 0L) {

            setCurrentState(FinishTaskState(0L, this))

        } else
            listFolder(rootFolder, { abstractRemoteFile, parentFolder -> doDownloadFolder(abstractRemoteFile, parentFolder) })

    }

    override fun cancelTask() {

        subDownloadTasks.forEach {

            it.cancelTask()

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

            Log.d(TAG, "download file name: " + abstractRemoteFile.name)

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