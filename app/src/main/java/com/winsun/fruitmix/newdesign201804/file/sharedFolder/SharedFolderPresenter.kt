package com.winsun.fruitmix.newdesign201804.file.sharedFolder

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.command.BaseAbstractCommand
import com.winsun.fruitmix.dialog.BottomMenuListDialogFactory
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.interfaces.BaseView
import com.winsun.fruitmix.model.BottomMenuItem
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.presenter.*
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderFileTitleViewModel
import com.winsun.fruitmix.newdesign201804.file.permissionManage.PermissionManageActivity

class SharedFolderPresenter(val fileDataSource: FileDataSource, val sharedFolderView: SharedFolderView) {


    fun initView(recyclerView: RecyclerView) {

        fileDataSource.getFile(object : BaseLoadDataCallback<AbstractRemoteFile> {

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                initRecyclerViewAdapter(recyclerView, data!!)

            }

            override fun onFail(operationResult: OperationResult?) {

            }

        })


    }

    private fun initRecyclerViewAdapter(recyclerView: RecyclerView, data: MutableList<AbstractRemoteFile>) {

        val fileRecyclerViewAdapter = FileRecyclerViewAdapter({ abstractRemoteFile, position ->
        }, {

        }, {
            showBottomDialogWhenClickMoreBtn(it)
        })

        fileRecyclerViewAdapter.currentOrientation = ORIENTATION_LIST_TYPE

        recyclerView.adapter = fileRecyclerViewAdapter

        val folderViewItems = mutableListOf<ViewItem>()
        val fileViewItems = mutableListOf<ViewItem>()

        data.forEach {

            if (it.isFolder) {
                folderViewItems.add(ItemFolder(it as RemoteFolder))
            } else {
                fileViewItems.add(ItemFile(it as RemoteFile))
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