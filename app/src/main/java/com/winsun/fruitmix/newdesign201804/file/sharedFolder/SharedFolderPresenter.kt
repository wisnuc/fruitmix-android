package com.winsun.fruitmix.newdesign201804.file.sharedFolder

import android.content.Intent
import android.support.v7.widget.RecyclerView
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.command.BaseAbstractCommand
import com.winsun.fruitmix.dialog.BottomMenuListDialogFactory
import com.winsun.fruitmix.file.data.model.*
import com.winsun.fruitmix.file.data.station.StationFileRepository
import com.winsun.fruitmix.model.BottomMenuItem
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.presenter.*
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FileItemViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderFileTitleViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderItemViewModel
import com.winsun.fruitmix.newdesign201804.file.permissionManage.PermissionManageActivity
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource

class SharedFolderPresenter(val fileDataSource: FileDataSource, val sharedFolderView: SharedFolderView) {

    private val sharedFolderRootUUID = "sharedFolderRootUUID"

    private val context = sharedFolderView.getContext()

    private val retrieveFolders = mutableListOf<AbstractRemoteFile>()

    private val currentUserUUID = InjectSystemSettingDataSource.provideSystemSettingDataSource(context).currentLoginUserUUID

    private lateinit var fileRecyclerViewAdapter: FileRecyclerViewAdapter

    private val shareRootDataUseCase = ShareRootDataUseCase(fileDataSource, currentUserUUID, context)

    fun initView(recyclerView: RecyclerView) {

        fileRecyclerViewAdapter = FileRecyclerViewAdapter({ file, position ->

            gotoNextFolder(file as AbstractRemoteFile)

        }, {

        }, { file, position ->
            showBottomDialogWhenClickMoreBtn(file, position)
        })

        fileRecyclerViewAdapter.currentOrientation = ORIENTATION_LIST_TYPE

        recyclerView.adapter = fileRecyclerViewAdapter

        shareRootDataUseCase.getRoot(object : BaseLoadDataCallback<AbstractRemoteFile> {

            override fun onFail(operationResult: OperationResult?) {

            }

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {
                val remoteFolder = RemoteFolder()
                remoteFolder.name = ""
                remoteFolder.uuid = sharedFolderRootUUID

                retrieveFolders.add(remoteFolder)

                if (data != null) {
                    refreshData(data)
                }

            }


        })

    }


    private fun gotoNextFolder(abstractRemoteFile: AbstractRemoteFile) {

        fileDataSource.getFile(abstractRemoteFile.rootFolderUUID, abstractRemoteFile.uuid, object : BaseLoadDataCallback<AbstractRemoteFile> {
            override fun onFail(operationResult: OperationResult?) {

            }

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                retrieveFolders.add(abstractRemoteFile)

                refreshData(data!!)

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

            shareRootDataUseCase.getRoot(object : BaseLoadDataCallback<AbstractRemoteFile> {
                override fun onFail(operationResult: OperationResult?) {

                }

                override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {
                    if (data != null) {
                        refreshData(data)
                    }
                }
            })

        } else {

            fileDataSource.getFile(preFolder.rootFolderUUID, preFolder.uuid, object : BaseLoadDataCallback<AbstractRemoteFile> {
                override fun onFail(operationResult: OperationResult?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                    retrieveFolders.remove(preFolder)
                    refreshData(data!!)

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

        val bottomMenuItems = mutableListOf<BottomMenuItem>()
        bottomMenuItems.add(BottomMenuItem(0, sharedFolderView.getString(R.string.permission_manage), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                val intent = Intent(sharedFolderView.getContext(), PermissionManageActivity::class.java)

                sharedFolderView.getContext().startActivity(intent)

            }

        }))

        bottomMenuItems.add(BottomMenuItem(0, sharedFolderView.getString(R.string.delete_text), object : BaseAbstractCommand() {

        }))

        BottomMenuListDialogFactory(bottomMenuItems).createDialog(sharedFolderView.getContext()).show()

    }

}