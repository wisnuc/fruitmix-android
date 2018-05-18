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
import com.winsun.fruitmix.newdesign201804.file.list.presenter.*
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FileItemViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderFileTitleViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderItemViewModel
import com.winsun.fruitmix.newdesign201804.file.permissionManage.PermissionManageActivity
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource

class SharedFolderPresenter(val stationFileRepository: StationFileRepository, val sharedFolderView: SharedFolderView) {

    private val sharedFolderRootUUID = "sharedFolderRootUUID"

    private val context = sharedFolderView.getContext()

    private var currentRootUUID = sharedFolderRootUUID
    private var currentFolderUUID = sharedFolderRootUUID

    private val currentUserUUID = InjectSystemSettingDataSource.provideSystemSettingDataSource(context).currentLoginUserUUID

    private val currentFolderItems = mutableListOf<AbstractRemoteFile>()

    private lateinit var fileRecyclerViewAdapter: FileRecyclerViewAdapter

    fun initView(recyclerView: RecyclerView) {

        fileRecyclerViewAdapter = FileRecyclerViewAdapter({ abstractRemoteFile, position ->
        }, {

        }, {
            showBottomDialogWhenClickMoreBtn(it)
        })

        fileRecyclerViewAdapter.currentOrientation = ORIENTATION_LIST_TYPE

        recyclerView.adapter = fileRecyclerViewAdapter

        getRoot(object : BaseOperateCallback {
            override fun onFail(operationResult: OperationResult?) {

            }

            override fun onSucceed() {
                refreshData(currentFolderItems)
            }
        })


    }

    private fun getRoot(baseOperateCallback: BaseOperateCallback) {

        stationFileRepository.getRootDrive(object : BaseLoadDataCallback<AbstractRemoteFile> {
            override fun onFail(operationResult: OperationResult?) {


            }

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                handleRoot(data!!, baseOperateCallback)
            }
        })

    }

    private var getFileCount = 0
    private var totalCount = 0

    private fun handleRoot(data: MutableList<AbstractRemoteFile>, baseOperateCallback: BaseOperateCallback) {

        val filterData = data.filter {

            if (it is RemotePublicDrive) {
                it.writeList.contains(currentUserUUID)
            } else it is RemoteBuiltInDrive

        }

        totalCount = filterData.size

        filterData.forEach {

            if (it is RemotePublicDrive) {

                val publicRootUUID = it.uuid

                stationFileRepository.getFile(publicRootUUID, publicRootUUID, object : BaseLoadDataCallback<AbstractRemoteFile> {
                    override fun onFail(operationResult: OperationResult?) {

                    }

                    override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                        data?.forEach {
                            it.rootFolderUUID = publicRootUUID
                            it.parentFolderUUID = publicRootUUID
                        }

                        if (data != null) {
                            currentFolderItems.addAll(data)
                        }

                        generateRootFolder(baseOperateCallback)

                    }
                })


            } else if (it is RemoteBuiltInDrive) {

                it.name = context.getString(R.string.built_in_drive)

                it.rootFolderUUID = it.uuid
                it.parentFolderUUID = it.uuid

                currentFolderItems.add(it)

                generateRootFolder(baseOperateCallback)

            }

        }

    }

    private fun generateRootFolder(baseOperateCallback: BaseOperateCallback) {

        getFileCount++

        if (getFileCount == totalCount)
            baseOperateCallback.onSucceed()


    }

    private fun gotoNextFolder() {

        if (currentRootUUID == sharedFolderRootUUID) {

        }

    }


    private fun gotoPreFolder() {


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

    private fun showBottomDialogWhenClickMoreBtn(abstractFile: AbstractFile) {

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