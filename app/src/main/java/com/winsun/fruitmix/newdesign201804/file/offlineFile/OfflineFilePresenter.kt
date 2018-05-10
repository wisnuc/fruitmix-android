package com.winsun.fruitmix.newdesign201804.file.offlineFile

import android.support.v7.widget.RecyclerView
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.file.data.model.*
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.file.list.presenter.*
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
        })

        fileRecyclerViewAdapter.currentOrientation = ORIENTATION_LIST_TYPE

        recyclerView.adapter = fileRecyclerViewAdapter

        val folderViewItems = mutableListOf<ViewItem>()
        val fileViewItems = mutableListOf<ViewItem>()

        data.forEach {

            if (it.isFolder) {
                folderViewItems.add(ItemFolder(it as LocalFolder))
            } else {
                fileViewItems.add(ItemFile(it as LocalFile))
            }

        }

        val viewItems = mutableListOf<ViewItem>()

        viewItems.addAll(folderViewItems)
        viewItems.addAll(fileViewItems)

        fileRecyclerViewAdapter.setItemList(viewItems)
        fileRecyclerViewAdapter.notifyDataSetChanged()

    }


}