package com.winsun.fruitmix.newdesign201804.file.sharedFolder

import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.View
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.*
import com.winsun.fruitmix.command.BaseAbstractCommand
import com.winsun.fruitmix.dialog.BottomMenuListDialogFactory
import com.winsun.fruitmix.file.data.model.*
import com.winsun.fruitmix.file.data.station.StationFileRepository
import com.winsun.fruitmix.model.BottomMenuItem
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.component.getCurrentUserUUID
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.presenter.*
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FileItemViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderFileTitleViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderItemViewModel
import com.winsun.fruitmix.newdesign201804.file.permissionManage.PermissionManageActivity
import com.winsun.fruitmix.newdesign201804.file.sharedFolder.data.SharedFolderDataSource
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource
import com.winsun.fruitmix.user.User
import com.winsun.fruitmix.user.datasource.InjectUser
import com.winsun.fruitmix.util.SnackbarUtil
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel

class SharedFolderPresenter(val fileDataSource: FileDataSource, val sharedFolderView: SharedFolderView,
                            val loadingViewModel: LoadingViewModel, val noContentViewModel: NoContentViewModel,
                            val sharedFolderDataSource: SharedFolderDataSource) {

    private val sharedFolderRootUUID = "sharedFolderRootUUID"

    private val context = sharedFolderView.getContext()

    private val retrieveFolders = mutableListOf<AbstractRemoteFile>()

    private val currentUserUUID = InjectSystemSettingDataSource.provideSystemSettingDataSource(context).currentLoginUserUUID

    private lateinit var fileRecyclerViewAdapter: FileRecyclerViewAdapter

    private val shareRootDataUseCase = ShareRootDataUseCase(fileDataSource, currentUserUUID, context)

    private val currentFolderItems = mutableListOf<AbstractRemoteFile>()

    fun initView(recyclerView: RecyclerView) {

        fileRecyclerViewAdapter = FileRecyclerViewAdapter({ file, position ->

            gotoNextFolder(file as AbstractRemoteFile)

        }, {

        }, { file, position ->
            showBottomDialogWhenClickMoreBtn(file, position)
        })

        fileRecyclerViewAdapter.currentOrientation = ORIENTATION_LIST_TYPE

        recyclerView.adapter = fileRecyclerViewAdapter

        showLoadingBg()

        shareRootDataUseCase.getRoot(object : LoadSharedFolderCallback() {

            override fun handleLoadSucceed() {
                val remoteFolder = RemoteFolder()
                remoteFolder.name = ""
                remoteFolder.uuid = sharedFolderRootUUID

                retrieveFolders.add(remoteFolder)
            }

            override fun showContent(data: MutableList<AbstractRemoteFile>, operationResult: OperationResult?) {

                currentFolderItems.addAll(data)

                refreshData(data)
            }

        })

    }

    private abstract inner class LoadSharedFolderCallback : BaseLoadDataCallback<AbstractRemoteFile> {
        override fun onFail(operationResult: OperationResult?) {
            showNoContentBg()

            SnackbarUtil.showSnackBar(sharedFolderView.getRootView(), Snackbar.LENGTH_SHORT, messageStr = operationResult!!.getResultMessage(context))
        }

        override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

            if (data == null)
                showNoContentBg()
            else {

                handleLoadSucceed()

                if (data.isEmpty())
                    showNoContentBg()
                else {

                    showContentBg()

                    showContent(data, operationResult)

                }

            }

        }

        abstract fun  handleLoadSucceed()

        abstract fun showContent(data: MutableList<AbstractRemoteFile>, operationResult: OperationResult?)

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

        sharedFolderView.setAddFabVisibility(View.INVISIBLE)

        showLoadingBg()

        fileDataSource.getFile(abstractRemoteFile.rootFolderUUID, abstractRemoteFile.uuid, object : LoadSharedFolderCallback() {

            override fun handleLoadSucceed() {
                retrieveFolders.add(abstractRemoteFile)
            }

            override fun showContent(data: MutableList<AbstractRemoteFile>, operationResult: OperationResult?) {
                refreshData(data)
            }

        })

    }

    fun onBackPressed() {
        gotoPreFolder()
    }

    fun notRoot(): Boolean {
        return retrieveFolders.last().uuid != sharedFolderRootUUID
    }

    private fun gotoPreFolder() {

        retrieveFolders.removeAt(retrieveFolders.lastIndex)

        val preFolder = retrieveFolders.last()

        if (preFolder.uuid == sharedFolderRootUUID) {

            sharedFolderView.setAddFabVisibility(View.VISIBLE)

            shareRootDataUseCase.getRoot(object : LoadSharedFolderCallback() {

                override fun handleLoadSucceed() {

                }

                override fun showContent(data: MutableList<AbstractRemoteFile>, operationResult: OperationResult?) {
                    refreshData(data)
                }

            })

        } else {

            sharedFolderView.setAddFabVisibility(View.INVISIBLE)

            fileDataSource.getFile(preFolder.rootFolderUUID, preFolder.uuid, object : LoadSharedFolderCallback() {

                override fun handleLoadSucceed() {
                    retrieveFolders.remove(preFolder)
                }

                override fun showContent(data: MutableList<AbstractRemoteFile>, operationResult: OperationResult?) {
                    refreshData(data)
                }

            })

        }

    }


    private fun refreshData(data: MutableList<AbstractRemoteFile>) {

        val folderViewItems = mutableListOf<ViewItem>()
        val fileViewItems = mutableListOf<ViewItem>()

        data.forEach {

            if (it.isFolder) {

                val folderItemViewModel = FolderItemViewModel(it as RemoteFolder)
                folderViewItems.add(ItemFolder(folderItemViewModel))
            } else {

                val fileItemViewModel = FileItemViewModel(it as RemoteFile)
                fileViewItems.add(ItemFile(fileItemViewModel))
            }

        }

        val fileTitleViewModel = FolderFileTitleViewModel()

        val viewItems = mutableListOf<ViewItem>()

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

    private fun showBottomDialogWhenClickMoreBtn(abstractFile: AbstractFile, position: Int) {

        if (abstractFile is RemoteBuiltInDrive)
            return

        val bottomMenuItems = mutableListOf<BottomMenuItem>()

        bottomMenuItems.add(BottomMenuItem(0, sharedFolderView.getString(R.string.permission_manage), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                val intent = Intent(sharedFolderView.getContext(), PermissionManageActivity::class.java)

                sharedFolderView.getContext().startActivity(intent)

            }

        }))

        bottomMenuItems.add(BottomMenuItem(0, sharedFolderView.getString(R.string.delete_text), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                handleDeleteSharedDisk(abstractFile)

            }

        }))

        BottomMenuListDialogFactory(bottomMenuItems).createDialog(sharedFolderView.getContext()).show()

    }

    private fun handleDeleteSharedDisk(abstractFile: AbstractFile) {

        AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete_or_not))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_text, { dialog, which ->

                    doDeleteSharedDisk(abstractFile)
                })

    }


    private fun doDeleteSharedDisk(abstractFile: AbstractFile) {

        sharedFolderView.showProgressDialog(context.getString(R.string.operating_title,
                context.getString(R.string.delete_text)))

        val abstractRemoteFile = abstractFile as AbstractRemoteFile

        sharedFolderDataSource.deleteSharedDisk(abstractRemoteFile.uuid, object : BaseOperateCallback {

            override fun onFail(operationResult: OperationResult?) {
                SnackbarUtil.showSnackBar(sharedFolderView.getRootView(), Snackbar.LENGTH_SHORT,
                        messageStr = operationResult!!.getResultMessage(context))
            }

            override fun onSucceed() {

                currentFolderItems.remove(abstractFile)

                refreshData(currentFolderItems)

                SnackbarUtil.showSnackBar(sharedFolderView.getRootView(), Snackbar.LENGTH_SHORT,
                        messageStr = context.getString(R.string.success, context.getString(R.string.delete_text)))

            }

        })

    }

    fun showAddSharedFolder() {

        InjectUser.provideRepository(context).getUsers(currentUserUUID, object : BaseLoadDataCallbackImpl<User>() {
            override fun onSucceed(data: MutableList<User>?, operationResult: OperationResult?) {
                super.onSucceed(data, operationResult)

                CreateSharedFolderDialog(data!!, { selectedUsers, diskName ->
                    doCreateSharedDisk(selectedUsers, diskName)
                }).createDialog(context)

            }
        })

    }

    private fun doCreateSharedDisk(users: List<User>, diskName: String) {

        sharedFolderView.showProgressDialog(context.getString(R.string.operating_title,
                context.getString(R.string.create)))

        sharedFolderDataSource.createSharedDisk(diskName, users, object : BaseOperateDataCallback<AbstractRemoteFile> {

            override fun onFail(operationResult: OperationResult?) {

                sharedFolderView.dismissDialog()

                SnackbarUtil.showSnackBar(sharedFolderView.getRootView(), Snackbar.LENGTH_SHORT,
                        messageStr = operationResult!!.getResultMessage(context))
            }

            override fun onSucceed(data: AbstractRemoteFile?, result: OperationResult?) {

                //TODO: refactor for forget call dismiss dialog

                sharedFolderView.dismissDialog()

                currentFolderItems.add(data!!)

                refreshData(currentFolderItems)

                SnackbarUtil.showSnackBar(sharedFolderView.getRootView(), Snackbar.LENGTH_SHORT,
                        messageStr = result!!.getResultMessage(context))

            }

        })

    }


}