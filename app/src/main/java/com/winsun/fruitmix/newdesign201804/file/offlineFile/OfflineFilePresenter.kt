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
import com.winsun.fruitmix.util.FileUtil
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel

class OfflineFilePresenter(private val offlineFileDataSource: OfflineFileDataSource,
                           val loadingViewModel: LoadingViewModel, val noContentViewModel: NoContentViewModel) {

    private val rootFolderPath = FileUtil.getDownloadFileStoreFolderPath()

    fun initView(recyclerView: RecyclerView) {

        loadingViewModel.showLoading.set(true)

        val filterPaths = listOf<String>(FileUtil.getAudioRecordFolderPath(), FileUtil.getLocalPhotoMiniThumbnailFolderPath(),
                FileUtil.getLocalPhotoThumbnailFolderPath(), FileUtil.getOriginalPhotoFolderPath())

        offlineFileDataSource.getFile(rootFolderPath, filterPaths, object : BaseLoadDataCallback<AbstractLocalFile> {

            override fun onSucceed(data: MutableList<AbstractLocalFile>?, operationResult: OperationResult?) {

                loadingViewModel.showLoading.set(false)

                if (data!!.isEmpty())
                    noContentViewModel.showNoContent.set(true)
                else {
                    noContentViewModel.showNoContent.set(false)

                    initRecyclerViewAdapter(recyclerView, data)
                }

            }

            override fun onFail(operationResult: OperationResult?) {

                loadingViewModel.showLoading.set(false)
                noContentViewModel.showNoContent.set(true)

            }

        })


    }

    private fun initRecyclerViewAdapter(recyclerView: RecyclerView, data: MutableList<AbstractLocalFile>) {

        val fileRecyclerViewAdapter = FileRecyclerViewAdapter({ file, position ->
        }, {

        }, { file, position ->

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