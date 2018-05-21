package com.winsun.fruitmix.newdesign201804.file.offlineFile

import android.support.v7.widget.RecyclerView
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.file.data.model.*
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.file.list.presenter.*
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FileItemViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderItemViewModel
import com.winsun.fruitmix.newdesign201804.file.offlineFile.data.OfflineFileDataSource

class OfflineFilePresenter(private val offlineFileDataSource: OfflineFileDataSource) {

    fun initView(recyclerView: RecyclerView){

        offlineFileDataSource.getFile(object :BaseLoadDataCallback<AbstractLocalFile>{

            override fun onSucceed(data: MutableList<AbstractLocalFile>?, operationResult: OperationResult?) {

                initRecyclerViewAdapter(recyclerView,data!!)

            }

            override fun onFail(operationResult: OperationResult?) {

            }

        })


    }

    private fun initRecyclerViewAdapter(recyclerView: RecyclerView,data: MutableList<AbstractLocalFile>){

        val fileRecyclerViewAdapter = FileRecyclerViewAdapter({ file, position ->
        }, {

        }, {
            file,position->

        })

        fileRecyclerViewAdapter.currentOrientation = ORIENTATION_LIST_TYPE

        recyclerView.adapter = fileRecyclerViewAdapter

        val folderViewItems = mutableListOf<ViewItem>()
        val fileViewItems = mutableListOf<ViewItem>()

        data.forEach {

            if (it.isFolder) {

                val folderItemViewModel = FolderItemViewModel(it as LocalFolder)
                folderItemViewModel.showOfflineAvailableIv.set(true)
                folderItemViewModel.showMoreBtn.set(false)

                folderViewItems.add(ItemFolder(folderItemViewModel))
            } else {

                val fileItemViewModel = FileItemViewModel(it as LocalFile)
                fileItemViewModel.showOfflineAvailableIv.set(true)
                fileItemViewModel.showMoreBtn.set(false)

                fileViewItems.add(ItemFile(fileItemViewModel))
            }

        }

        val viewItems = mutableListOf<ViewItem>()

        viewItems.addAll(folderViewItems)
        viewItems.addAll(fileViewItems)

        fileRecyclerViewAdapter.setItemList(viewItems)
        fileRecyclerViewAdapter.notifyDataSetChanged()

    }


}