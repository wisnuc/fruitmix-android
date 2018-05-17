package com.winsun.fruitmix.newdesign201804.file.move

import android.support.v7.widget.LinearLayoutManager
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.databinding.ActivityMoveFileBinding
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.presenter.*
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FileItemViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderFileTitleViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderItemViewModel

class MoveFilePresenter(val fileDataSource: FileDataSource, val activityMoveFileBinding: ActivityMoveFileBinding) {

    private val context = activityMoveFileBinding.cancelBtn.context

    private val viewItems = mutableListOf<ViewItem>()

    private val currentFolderItems = mutableListOf<AbstractRemoteFile>()

    fun initView() {

        fileDataSource.getFile("","",object : BaseLoadDataCallback<AbstractRemoteFile> {

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                currentFolderItems.addAll(data!!)

                initRecyclerViewAdapter()

            }

            override fun onFail(operationResult: OperationResult?) {

            }

        })

    }

    private fun initRecyclerViewAdapter() {

        val fileRecyclerViewAdapter = FileRecyclerViewAdapter({ abstractRemoteFile, position ->
        }, {

        }, {

        })

        fileRecyclerViewAdapter.currentOrientation = ORIENTATION_LIST_TYPE

        activityMoveFileBinding.fileRecyclerView.layoutManager = LinearLayoutManager(context)
        activityMoveFileBinding.fileRecyclerView.adapter = fileRecyclerViewAdapter

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

                fileViewItems.add(ItemFile(fileItemViewModel))
            }

        }

        val fileTitleViewModel = FolderFileTitleViewModel()

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


}