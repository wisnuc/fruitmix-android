package com.winsun.fruitmix.newdesign201804.file.move

import android.support.v7.widget.LinearLayoutManager
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
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
import com.winsun.fruitmix.newdesign201804.file.sharedFolder.ShareRootDataUseCase
import com.winsun.fruitmix.viewmodel.ToolbarViewModel

private const val rootUUID = "rootUUID"

class MoveFilePresenter(val fileDataSource: FileDataSource, val activityMoveFileBinding: ActivityMoveFileBinding,
                        val toolbarViewModel: ToolbarViewModel) {

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

        })

        fileRecyclerViewAdapter.currentOrientation = ORIENTATION_LIST_TYPE

        activityMoveFileBinding.fileRecyclerView.layoutManager = LinearLayoutManager(context)
        activityMoveFileBinding.fileRecyclerView.adapter = fileRecyclerViewAdapter

        getRoot()

        moveBtn.setOnClickListener {
            doMove()
        }

    }

    private var getFileCount = 0
    private val totalCount = 2

    private fun getRoot() {

        getFileCount = 0

        fileDataSource.getFile(currentUserHome, currentUserHome, object : BaseLoadDataCallback<AbstractRemoteFile> {

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                data?.forEach {
                    it.rootFolderUUID = currentUserHome
                }

                currentFolderItems.addAll(data!!)

                handleGetFileSucceed()

            }

            override fun onFail(operationResult: OperationResult?) {

            }

        })

        shareRootDataUseCase.getRoot(object : BaseLoadDataCallback<AbstractRemoteFile> {

            override fun onFail(operationResult: OperationResult?) {

            }

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                currentFolderItems.addAll(data!!)

                handleGetFileSucceed()

            }
        })

    }

    private fun handleGetFileSucceed() {

        getFileCount++

        if (getFileCount == totalCount) {

            val remoteFolder = RemoteFolder()
            remoteFolder.uuid = rootUUID

            retrievedFolders.add(remoteFolder)

            refreshData()
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

        fileDataSource.getFile(abstractRemoteFile.rootFolderUUID, abstractRemoteFile.uuid, object : BaseLoadDataCallback<AbstractRemoteFile> {

            override fun onFail(operationResult: OperationResult?) {

            }

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                currentFolderItems.clear()
                currentFolderItems.addAll(data!!)

                retrievedFolders.add(abstractRemoteFile)

                refreshData()

                refreshTitle()

            }

        })

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

            fileDataSource.getFile(remoteFile.rootFolderUUID, remoteFile.uuid, object : BaseLoadDataCallback<AbstractRemoteFile> {
                override fun onFail(operationResult: OperationResult?) {

                }

                override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                    currentFolderItems.clear()
                    currentFolderItems.addAll(data!!)

                    refreshData()

                    refreshTitle()

                }
            })

        }

    }

    private fun refreshTitle() {

        val remoteFile = retrievedFolders.last()

        if (remoteFile.uuid == rootUUID)
            toolbarViewModel.titleText.set(context.getString(R.string.move_to))
        else
            toolbarViewModel.titleText.set(remoteFile.name)

    }

    private fun refreshMoveState(abstractFile: AbstractFile) {


    }

    private fun doMove() {


    }


}