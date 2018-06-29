package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

import android.util.Log
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.file.data.model.AbstractLocalFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.LocalFile
import com.winsun.fruitmix.file.data.model.LocalFolder
import com.winsun.fruitmix.file.data.station.StationFileRepository
import com.winsun.fruitmix.http.HttpResponse
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.model.operationResult.OperationSuccessWithFile
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.data.FileUploadParam
import com.winsun.fruitmix.parser.RemoteMkDirParser
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.util.Util
import java.io.File

private const val TAG = "UploadFolderTask"

class UploadFolderTask(
        val stationFileRepository: StationFileRepository,
        uuid: String, createUserUUID: String, private val abstractLocalFile: AbstractLocalFile, fileDataSource: FileDataSource,
        fileUploadParam: FileUploadParam, threadManager: ThreadManager) :
        UploadTask(uuid, createUserUUID, abstractLocalFile, fileDataSource, fileUploadParam, threadManager) {

    private var totalSize = 0L
    private val subUploadTasks = mutableMapOf<TaskStateObserver, Task>()

    private val remoteMkDirParser = RemoteMkDirParser()

    private lateinit var startingTaskState: StartingTaskState

    private var uploadFinishFileSize = 0L

    override fun executeTask() {

        totalSize = 0L
        uploadFinishFileSize = 0L
        subUploadTasks.clear()

        val uploadFolderCallable = OperateFileCallable {

            val folder = File(abstractLocalFile.path)

            analyseFolder(folder)

            Log.d(TAG, "totalSize: $totalSize")

            startingTaskState = StartingTaskState(0, totalSize, "0KB/s", this)

            setCurrentState(startingTaskState)

            if (totalSize == 0L) {
                setCurrentState(FinishTaskState(0L, this))
            } else
                doUploadFolder(arrayOf(folder), fileUploadParam.driveUUID, fileUploadParam.dirUUID)

        }

        future = threadManager.runOnCacheThread(uploadFolderCallable)

    }

    override fun doCancelTask() {

        subUploadTasks.forEach { (taskStateObserver, task) ->

            task.unregisterObserver(taskStateObserver)
            task.cancelTask()

        }

        super.doCancelTask()

    }

    override fun setCurrentState(taskState: TaskState) {
        super.setCurrentState(taskState)

        if (taskState is PauseTaskState) {

            subUploadTasks.forEach {

                it.value.pauseTask()

            }

        }

    }

    private fun analyseFolder(parent: File) {

        listFolder(parent,
                { folder ->
                    analyseFolder(folder)
                },
                { file ->
                    Log.d(TAG, "file name:${file.name} size:${file.length()}")
                    totalSize += file.length()
                })
    }

    private fun listFolder(folder: File, handleItemIsDirectory: (folder: File) -> Unit,
                           handleItemIsFile: (file: File) -> Unit) {

        val folderItems = folder.listFiles()

        folderItems.forEach {

            if (it.isDirectory)
                handleItemIsDirectory(it)
            else {
                handleItemIsFile(it)
            }

        }

    }

    private fun doUploadFolder(files: Array<File>, driveUUID: String, parentFolderUUID: String) {

        val operationResult = stationFileRepository.getFileWithoutCreateNewThread(driveUUID, parentFolderUUID, "")

        val alreadyExistFiles = mutableListOf<AbstractRemoteFile>()

        if (operationResult is OperationSuccessWithFile)
            alreadyExistFiles.addAll(operationResult.list)

        files.forEach {

            if (it.isDirectory) {

                var directoryFolderUUID = ""

                for (i in 0 until alreadyExistFiles.size) {

                    val alreadyExistFile = alreadyExistFiles[i]
                    if (alreadyExistFile.name == it.name) {
                        directoryFolderUUID = alreadyExistFile.uuid
                        break
                    }

                }

                if (directoryFolderUUID.isNotEmpty()) {
                    doUploadFolder(it.listFiles(), driveUUID, directoryFolderUUID)
                    return
                }

                val localFolder = LocalFolder()
                localFolder.name = it.name

                val fileUploadParam = FileUploadParam(driveUUID, parentFolderUUID, localFolder)

                Log.d(TAG, "start create folder: " + localFolder.name)

                doCreateFolder(fileUploadParam, object : BaseOperateDataCallback<String> {
                    override fun onFail(operationResult: OperationResult?) {

                        Log.d(TAG, "create folder " + localFolder.name + " failed")

                        deleteTask()
                        setCurrentState(ErrorTaskState(this@UploadFolderTask))
                    }

                    override fun onSucceed(data: String, result: OperationResult?) {

                        doUploadFolder(it.listFiles(), driveUUID, data)

                    }
                })


            } else {

                var fileExist = false

                for (i in 0 until alreadyExistFiles.size) {

                    val alreadyExistFile = alreadyExistFiles[i]
                    if (alreadyExistFile.name == it.name) {

                        fileExist = true

                        handleFileUploadSucceed(it.length())

                        break
                    }

                }

                if (fileExist) {
                    return
                }

                val localFile = LocalFile()
                localFile.name = it.name

                localFile.path = it.absolutePath

                val fileHash = Util.calcSHA256OfFile(localFile.path)

                localFile.fileHash = fileHash
                localFile.size = it.length()

                val fileUploadParam = FileUploadParam(driveUUID, parentFolderUUID, localFile)

                val uploadTask = UploadTask(Util.createLocalUUid(), createUserUUID,
                        localFile, fileDataSource, fileUploadParam, threadManager)

                val downloadUploadFolderTaskStateObserver = object : TaskStateObserver {

                    override fun notifyStateChanged(currentState: TaskState) {

                        if (currentState is StartingTaskState) {

                            Log.d(TAG, "addCurrentHandleFileSize: " + currentState.speedSize + " fileName: " + localFile.name)

                            startingTaskState.addCurrentHandleFileSize(currentState.speedSize)

                            Log.d(TAG, "currentHandleFileSize:${startingTaskState.currentHandledSize} totalSize:$totalSize")

                            setCurrentState(startingTaskState)

                        } else if (currentState is FinishTaskState) {

                            handleFileUploadSucceed(localFile.size)

                            Log.d(TAG, "file: " + localFile.name + " finish download," +
                                    "uploadFinishFileSize: " + uploadFinishFileSize + " totalSize: " + totalSize)

                            uploadTask.unregisterObserver(this)

                        } else if (currentState is ErrorTaskState) {

                            Log.d(TAG, "file: " + localFile.name + " error occur when download")

                            deleteTask()
                            setCurrentState(ErrorTaskState(this@UploadFolderTask))

                        }

                    }

                }

                uploadTask.registerObserver(downloadUploadFolderTaskStateObserver)

                subUploadTasks[downloadUploadFolderTaskStateObserver] = uploadTask

                uploadTask.init()
                uploadTask.startTask()

                Log.d(TAG, "start upload file: " + localFile.path)

            }


        }


    }

    private fun handleFileUploadSucceed(fileSize: Long) {
        uploadFinishFileSize += fileSize

        if (uploadFinishFileSize == totalSize)
            setCurrentState(FinishTaskState(totalSize, this@UploadFolderTask))

    }


    private fun doCreateFolder(fileUploadParam: FileUploadParam, baseOperateDataCallback: BaseOperateDataCallback<String>) {

        val folderName = fileUploadParam.abstractLocalFile.name

        val rootUUID = fileUploadParam.driveUUID
        val parentFolderUUID = fileUploadParam.dirUUID

        stationFileRepository.createFolderWithoutCreateNewThread(folderName, rootUUID, parentFolderUUID,
                object : BaseOperateDataCallback<HttpResponse> {
                    override fun onFail(operationResult: OperationResult?) {

                        baseOperateDataCallback.onFail(operationResult)
                    }

                    override fun onSucceed(data: HttpResponse?, result: OperationResult?) {

                        val file = remoteMkDirParser.parse(data?.responseData)

                        baseOperateDataCallback.onSucceed(file.uuid, OperationSuccess())

                    }
                })

    }


}