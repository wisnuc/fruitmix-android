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
import com.winsun.fruitmix.file.data.model.*
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.util.getCurrentUserHome
import com.winsun.fruitmix.newdesign201804.util.getCurrentUserUUID
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.presenter.*
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FileItemViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderFileTitleViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderItemViewModel
import com.winsun.fruitmix.newdesign201804.file.operation.CreateFolderUseCase
import com.winsun.fruitmix.newdesign201804.file.operation.SortFileUseCase
import com.winsun.fruitmix.newdesign201804.file.sharedFolder.ShareRootDataUseCase
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.TransmissionTaskDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.TASK_UUID_KEY
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task
import com.winsun.fruitmix.newdesign201804.user.preference.*
import com.winsun.fruitmix.util.SnackbarUtil
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel
import com.winsun.fruitmix.viewmodel.ToolbarViewModel
import java.util.*
import kotlin.Comparator

private const val rootUUID = "rootUUID"

private const val sharedFolderRootUUID = "sharedFolderRootUUID"

class MoveFilePresenter(val fileDataSource: FileDataSource, val transmissionTaskDataSource: TransmissionTaskDataSource,
                        val activityMoveFileBinding: ActivityMoveFileBinding,
                        val toolbarViewModel: ToolbarViewModel, val moveFileView: MoveFileView,
                        val loadingViewModel: LoadingViewModel, val noContentViewModel: NoContentViewModel,
                        val fileOperation: Int, val userPreference: UserPreference,
                        val userPreferenceDataSource: UserPreferenceDataSource) {

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

    private var currentRootUUID = ""

    private lateinit var sortComparator: Comparator<ItemContent>

    fun initView() {

        val sortFileUseCase = SortFileUseCase(userPreference, currentUserUUID, userPreferenceDataSource,
                {
                    refreshData()
                })

        fileRecyclerViewAdapter = FileRecyclerViewAdapter({ abstractRemoteFile, position ->

            gotoNextFolder(abstractRemoteFile as AbstractRemoteFile)

        }, {
        }, { abstractRemoteFile, position ->

        }, handleSortBtnOnClick = {

            sortFileUseCase.showSortBottomDialog(context, false)

        }, sortPolicy = userPreference.fileSortPolicy)

        sortComparator = sortFileUseCase.getSortComparator()

        fileRecyclerViewAdapter.fileViewMode = FileViewMode.LIST

        activityMoveFileBinding.fileRecyclerView.layoutManager = LinearLayoutManager(context)
        activityMoveFileBinding.fileRecyclerView.adapter = fileRecyclerViewAdapter

        if (fileOperation == FILE_OPERATE_MOVE) {

            moveBtn.text = context.getText(R.string.move_to)

            moveBtn.setOnClickListener {
                doMove()
            }

            currentRootUUID = rootUUID

        } else if (fileOperation == FILE_OPERATE_COPY) {

            moveBtn.text = context.getText(R.string.copy_to)

            moveBtn.setOnClickListener {
                doCopy()
            }

            currentRootUUID = rootUUID

        } else if (fileOperation == FILE_OPERATE_SHARE_TO_SHARED_FOLDER) {

            moveBtn.text = context.getText(R.string.copy_to)

            moveBtn.setOnClickListener {
                doCopy()
            }

            currentRootUUID = sharedFolderRootUUID

        }

        getRoot()

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
    private var totalCount = 0

    private fun getRoot() {

        val abstractFile: AbstractRemoteFile

        if (currentRootUUID == sharedFolderRootUUID)
            abstractFile = getSharedRootFolder()
        else {
            abstractFile = RemoteFolder()
            abstractFile.uuid = currentRootUUID
            abstractFile.name = ""
        }

        gotoNextFolder(abstractFile)

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

    private fun gotoNextFolder(abstractRemoteFile: AbstractRemoteFile) {

        showLoadingBg()

        if (abstractRemoteFile.uuid == rootUUID) {
            gotoRootFolder(abstractRemoteFile, true)

        } else if (abstractRemoteFile.uuid == sharedFolderRootUUID) {

            gotoSharedRootFolder(abstractRemoteFile, true)

        } else {

            fileDataSource.getFile(abstractRemoteFile.rootFolderUUID, abstractRemoteFile.uuid, abstractRemoteFile.name,
                    object : BaseLoadDataCallback<AbstractRemoteFile> {

                        override fun onFail(operationResult: OperationResult?) {

                            showNoContentBg()

                            handleGetFileSucceed(Collections.emptyList())

                        }

                        override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                            retrievedFolders.add(abstractRemoteFile)

                            handleGetFileSucceed(data)

                        }

                    })

        }

    }

    private fun gotoPreFolder() {

        retrievedFolders.removeAt(retrievedFolders.lastIndex)

        val remoteFile = retrievedFolders.last()

        showLoadingBg()

        if (remoteFile.uuid == rootUUID) {

            gotoRootFolder(remoteFile, false)

        } else if (remoteFile.uuid == sharedFolderRootUUID) {

            gotoSharedRootFolder(remoteFile, false)

        } else {

            fileDataSource.getFile(remoteFile.rootFolderUUID, remoteFile.uuid,remoteFile.name, object : BaseLoadDataCallback<AbstractRemoteFile> {
                override fun onFail(operationResult: OperationResult?) {
                    showNoContentBg()

                    handleGetFileSucceed(Collections.emptyList())
                }

                override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                    handleGetFileSucceed(data)

                }
            })

        }

    }

    private fun gotoRootFolder(abstractRemoteFile: AbstractRemoteFile, addIntoRetrievedFolders: Boolean) {

        val privateDrive = RemotePrivateDrive()

        privateDrive.rootFolderUUID = currentUserHome
        privateDrive.uuid = currentUserHome

        privateDrive.name = moveFileView.getString(R.string.my_private_drive)

        val sharedFolderRootDrive = getSharedRootFolder()

        val folderItems = mutableListOf<AbstractRemoteFile>(
                privateDrive, sharedFolderRootDrive
        )

        handleGetFileFinish(abstractRemoteFile, folderItems, addIntoRetrievedFolders)

    }

    private fun getSharedRootFolder(): RemoteFolder {
        val sharedFolderRootDrive = RemoteFolder()
        sharedFolderRootDrive.rootFolderUUID = sharedFolderRootUUID
        sharedFolderRootDrive.uuid = sharedFolderRootUUID

        sharedFolderRootDrive.name = moveFileView.getString(R.string.shared_folder)
        return sharedFolderRootDrive
    }

    private fun gotoSharedRootFolder(abstractRemoteFile: AbstractRemoteFile, addIntoRetrievedFolders: Boolean) {

        shareRootDataUseCase.getRoot(object : BaseLoadDataCallback<AbstractRemoteFile> {

            override fun onFail(operationResult: OperationResult?) {

                showNoContentBg()

            }

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                currentFolderItems.addAll(data!!)

                handleGetFileFinish(abstractRemoteFile, data, addIntoRetrievedFolders)

            }
        })

    }

    private fun handleGetFileFinish(abstractRemoteFile: AbstractRemoteFile, data: MutableList<AbstractRemoteFile>?,
                                    addIntoRetrievedFolders: Boolean) {

        if (addIntoRetrievedFolders) {

            retrievedFolders.add(abstractRemoteFile)
        }

        handleGetFileSucceed(data)

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

        }

        refreshTitle()

        refreshMoveState(selectFiles[0] as AbstractRemoteFile)
    }


    private fun refreshData() {

        val folderViewItems = mutableListOf<ItemContent>()
        val fileViewItems = mutableListOf<ItemContent>()

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

        folderViewItems.sortWith(sortComparator)
        fileViewItems.sortWith(sortComparator)

        if (userPreference.fileSortPolicy.getCurrentSortDirection() == SortDirection.NEGATIVE) {
            folderViewItems.reverse()
            fileViewItems.reverse()
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


    fun notRoot(): Boolean {

        return retrievedFolders.last().uuid != currentRootUUID

    }

    fun onBackPressed() {
        gotoPreFolder()
    }


    private fun refreshTitle() {

        val remoteFile = retrievedFolders.last()

        when {
            remoteFile.uuid == rootUUID -> {

                toolbarViewModel.navigationIconResId.set(R.drawable.close)

                toolbarViewModel.titleText.set(moveFileView.getString(R.string.select_target_location))

                toolbarViewModel.showMenu.set(false)

            }
            else -> {

                toolbarViewModel.navigationIconResId.set(R.drawable.back_black)

                toolbarViewModel.titleText.set(remoteFile.name)

                if (remoteFile.uuid == sharedFolderRootUUID) {
                    toolbarViewModel.showMenu.set(false)
                } else {
                    toolbarViewModel.showMenu.set(true)
                }

            }
        }

    }

    private fun refreshMoveState(abstractRemoteFile: AbstractRemoteFile) {

        val currentRetrievedFolder = retrievedFolders.last()

        if (currentRetrievedFolder.uuid != rootUUID && currentRetrievedFolder.uuid != sharedFolderRootUUID
                && abstractRemoteFile.parentFolderUUID != currentRetrievedFolder.uuid) {

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