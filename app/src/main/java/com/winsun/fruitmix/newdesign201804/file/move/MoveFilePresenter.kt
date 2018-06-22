package com.winsun.fruitmix.newdesign201804.file.move

import android.app.Activity
import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.databinding.ActivityMoveFileBinding
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.component.getCurrentUserHome
import com.winsun.fruitmix.newdesign201804.component.getCurrentUserUUID
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.presenter.*
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FileItemViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderFileTitleViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderItemViewModel
import com.winsun.fruitmix.newdesign201804.file.operation.CreateFolderUseCase
import com.winsun.fruitmix.newdesign201804.file.sharedFolder.ShareRootDataUseCase
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.TransmissionTaskDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.TASK_UUID_KEY
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.newdesign201804.user.preference.FileViewMode
import com.winsun.fruitmix.newdesign201804.user.preference.UserPreferenceContainer
import com.winsun.fruitmix.util.SnackbarUtil
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel
import com.winsun.fruitmix.viewmodel.ToolbarViewModel
import java.util.*

private const val rootUUID = "rootUUID"

class MoveFilePresenter(val fileDataSource: FileDataSource, val transmissionTaskDataSource: TransmissionTaskDataSource,
                        val activityMoveFileBinding: ActivityMoveFileBinding,
                        val toolbarViewModel: ToolbarViewModel, val moveFileView: MoveFileView,
                        val loadingViewModel: LoadingViewModel, val noContentViewModel: NoContentViewModel,
                        val fileOperation: Int) {

    private val context = activityMoveFileBinding.cancelBtn.context

    private val viewItems = mutableListOf<ViewItem>()

    private val currentFolderItems = mutableListOf<AbstractRemoteFile>()

    private lateinit var fileRecyclerViewAdapter: FileRecyclerViewAdapter

    private val currentUserUUID = context.getCurrentUserUUID()

    private val currentUserHome = context.getCurrentUserHome()

    private val shareRootDataUseCase = ShareRootDataUseCase(fileDataSource, currentUserUUID, context)

    private val retrievedFolders = mutableListOf<AbstractRemoteFile>()

    private val moveBtn = activityMoveFileBinding.moveBtn

    private val selectFiles = SelectMoveFileDataSource.getSelectFiles()

    fun initView() {

        fileRecyclerViewAdapter = FileRecyclerViewAdapter({ abstractRemoteFile, position ->

            gotoNextFolder(abstractRemoteFile as AbstractRemoteFile)

        }, {

        }, { abstractRemoteFile, position ->

        }, sortPolicy = UserPreferenceContainer.userPreference.fileSortPolicy)

        fileRecyclerViewAdapter.fileViewMode = FileViewMode.LIST

        activityMoveFileBinding.fileRecyclerView.layoutManager = LinearLayoutManager(context)
        activityMoveFileBinding.fileRecyclerView.adapter = fileRecyclerViewAdapter

        getRoot()

        if (fileOperation == FILE_OPERATE_MOVE) {

            moveBtn.text = context.getText(R.string.move_to)

            moveBtn.setOnClickListener {
                doMove()
            }

        } else if (fileOperation == FILE_OPERATE_COPY) {

            moveBtn.text = context.getText(R.string.copy_to)

            moveBtn.setOnClickListener {
                doCopy()
            }

        } else {

            moveBtn.text = context.getText(R.string.copy_to)

            moveBtn.setOnClickListener {
                doCopy()
            }

        }

    }

    fun handleCreateFolderBtnOnClick() {

        val createFolderUseCase = CreateFolderUseCase(fileDataSource, moveFileView, moveBtn,
                {

                    newFolder ->

                    currentFolderItems.add(newFolder)

                    refreshView()

                })

        createFolderUseCase.createFolder(context, retrievedFolders.last())

    }

    private var getFileCount = 0
    private var totalCount = 2

    private fun getRoot() {

        getFileCount = 0

        totalCount = if (fileOperation == FILE_OPERATE_SHARE_TO_SHARED_FOLDER)
            1
        else
            2

        showLoadingBg()

        if (fileOperation != FILE_OPERATE_SHARE_TO_SHARED_FOLDER) {
            fileDataSource.getFile(currentUserHome, currentUserHome, object : BaseLoadDataCallback<AbstractRemoteFile> {

                override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                    data?.forEach {
                        it.rootFolderUUID = currentUserHome
                    }

                    currentFolderItems.addAll(data!!)

                    handleGetFileFinish()

                }

                override fun onFail(operationResult: OperationResult?) {

                    handleGetFileFinish()

                }

            })
        }

        shareRootDataUseCase.getRoot(object : BaseLoadDataCallback<AbstractRemoteFile> {

            override fun onFail(operationResult: OperationResult?) {

                handleGetFileFinish()

            }

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                currentFolderItems.addAll(data!!)

                handleGetFileFinish()

            }
        })

    }

    private fun showNoContentBg() {
        loadingViewModel.showLoading.set(false)
        noContentViewModel.showNoContent.set(true)
    }

    private fun showLoadingBg() {
        loadingViewModel.showLoading.set(true)
    }

    private fun showContentBg() {
        loadingViewModel.showLoading.set(false)
        noContentViewModel.showNoContent.set(false)
    }

    private fun handleGetFileFinish() {

        getFileCount++

        if (getFileCount == totalCount) {

            val remoteFolder = RemoteFolder()
            remoteFolder.uuid = rootUUID

            retrievedFolders.add(remoteFolder)

            refreshView()

        }

    }

    private fun refreshData() {

        val folderViewItems = mutableListOf<ViewItem>()
        val fileViewItems = mutableListOf<ViewItem>()

        currentFolderItems.forEach {

            if (it.isFolder) {

                val folderItemViewModel = FolderItemViewModel(it as RemoteFolder)
                folderItemViewModel.showMoreBtn.set(false)

                folderViewItems.add(ItemFolder(folderItemViewModel))
            } else {

                val fileItemViewModel = FileItemViewModel(it as RemoteFile)
                fileItemViewModel.showMoreBtn.set(false)
                fileItemViewModel.isDisable.set(true)

                fileViewItems.add(ItemFile(fileItemViewModel))
            }

        }

        val fileTitleViewModel = FolderFileTitleViewModel()

        viewItems.clear()

        if (folderViewItems.size > 0) {

            val folderFileTitleViewModel = FolderFileTitleViewModel()
            folderFileTitleViewModel.showSortBtn.set(true)

            viewItems.add(ItemFolderHead(folderFileTitleViewModel))
            viewItems.addAll(folderViewItems)

            fileTitleViewModel.showSortBtn.set(false)
            viewItems.add(ItemFileHead(fileTitleViewModel))
            viewItems.addAll(fileViewItems)

        } else {

            fileTitleViewModel.showSortBtn.set(true)
            viewItems.add(ItemFileHead(fileTitleViewModel))
            viewItems.addAll(fileViewItems)

        }

        fileRecyclerViewAdapter.setItemList(viewItems)
        fileRecyclerViewAdapter.notifyDataSetChanged()

    }

    private fun gotoNextFolder(abstractRemoteFile: AbstractRemoteFile) {


        showLoadingBg()

        fileDataSource.getFile(abstractRemoteFile.rootFolderUUID, abstractRemoteFile.uuid, object : BaseLoadDataCallback<AbstractRemoteFile> {

            override fun onFail(operationResult: OperationResult?) {

                showNoContentBg()

                refreshMoveState(selectFiles[0] as AbstractRemoteFile)

            }

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                retrievedFolders.add(abstractRemoteFile)

                handleGetFileSucceed(data)

            }

        })

    }

    private fun handleGetFileSucceed(data: MutableList<AbstractRemoteFile>?) {

        currentFolderItems.clear()

        currentFolderItems.addAll(data!!)

        refreshView()

    }

    private fun refreshView() {

        if (currentFolderItems.isEmpty())
            showNoContentBg()
        else {
            showContentBg()

            refreshData()

            refreshTitle()
        }

        refreshMoveState(selectFiles[0] as AbstractRemoteFile)
    }

    fun notRoot(): Boolean {

        return retrievedFolders.last().uuid != rootUUID

    }

    fun onBackPressed() {
        gotoPreFolder()
    }

    private fun gotoPreFolder() {

        retrievedFolders.removeAt(retrievedFolders.lastIndex)

        val remoteFile = retrievedFolders.last()

        if (remoteFile.uuid == rootUUID) {
            getRoot()
            refreshTitle()
        } else {

            showLoadingBg()

            fileDataSource.getFile(remoteFile.rootFolderUUID, remoteFile.uuid, object : BaseLoadDataCallback<AbstractRemoteFile> {
                override fun onFail(operationResult: OperationResult?) {
                    showNoContentBg()

                    refreshMoveState(selectFiles[0] as AbstractRemoteFile)
                }

                override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                    handleGetFileSucceed(data)

                }
            })

        }

    }

    private fun refreshTitle() {

        val remoteFile = retrievedFolders.last()

        if (remoteFile.uuid == rootUUID)
            toolbarViewModel.titleText.set(moveFileView.getRootTitleText())
        else
            toolbarViewModel.titleText.set(remoteFile.name)

    }

    private fun refreshMoveState(abstractRemoteFile: AbstractRemoteFile) {

        val currentRetrievedFolder = retrievedFolders.last()

        if (currentRetrievedFolder.uuid != rootUUID && abstractRemoteFile.parentFolderUUID != currentRetrievedFolder.uuid) {

            moveBtn.isEnabled = true

            moveBtn.setTextColor(ContextCompat.getColor(context, R.color.new_design_primary_color))

        } else {
            moveBtn.isEnabled = false

            moveBtn.setTextColor(ContextCompat.getColor(context, R.color.twenty_six_percent_black))
        }

    }

    private fun doMove() {

        val (srcFile, targetFile, entries) = doFileOperate()

        moveFileView.showProgressDialog(context.getString(R.string.operating_title, context.getString(R.string.create_move_task)))

        fileDataSource.moveFile(srcFile as AbstractRemoteFile, targetFile,
                entries, object : BaseOperateDataCallback<Task> {

            override fun onFail(operationResult: OperationResult?) {

                moveFileView.dismissDialog()

                SnackbarUtil.showSnackBar(moveBtn, Snackbar.LENGTH_SHORT, messageStr = operationResult!!.getResultMessage(context))
            }

            override fun onSucceed(task: Task, operationResult: OperationResult) {

                transmissionTaskDataSource.addTransmissionTask(task)

                moveFileView.dismissDialog()

                val intent = Intent()
                intent.putExtra(TASK_UUID_KEY, task.uuid)

                moveFileView.setResult(Activity.RESULT_OK, intent)

                moveFileView.finishView()

            }
        })

    }

    private fun doFileOperate(): Triple<AbstractFile, RemoteFolder, MutableList<AbstractRemoteFile>> {
        val srcFile = selectFiles[0]

        val targetFile = RemoteFolder()
        val currentRetrievedFolder = retrievedFolders.last()

        targetFile.rootFolderUUID = currentRetrievedFolder.rootFolderUUID
        targetFile.parentFolderUUID = currentRetrievedFolder.uuid

        val entries = mutableListOf<AbstractRemoteFile>()
        selectFiles.forEach {
            entries.add(it as AbstractRemoteFile)
        }
        return Triple(srcFile, targetFile, entries)
    }


    private fun doCopy() {

        val (srcFile, targetFile, entries) = doFileOperate()

        moveFileView.showProgressDialog(context.getString(R.string.operating_title, context.getString(R.string.create_copy_task)))

        fileDataSource.copyFile(srcFile as AbstractRemoteFile, targetFile,
                entries, object : BaseOperateDataCallback<Task> {
            override fun onFail(operationResult: OperationResult?) {

                moveFileView.dismissDialog()

                SnackbarUtil.showSnackBar(moveBtn, Snackbar.LENGTH_SHORT, messageStr = operationResult!!.getResultMessage(context))

            }

            override fun onSucceed(task: Task, operationResult: OperationResult) {

                transmissionTaskDataSource.addTransmissionTask(task)

                moveFileView.dismissDialog()

                val intent = Intent()
                intent.putExtra(TASK_UUID_KEY, task.uuid)

                moveFileView.setResult(Activity.RESULT_OK, intent)

                moveFileView.finishView()

            }
        })


    }

}