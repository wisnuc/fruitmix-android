package com.winsun.fruitmix.newdesign201804.file.offlineFile

import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.file.data.model.*
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.util.inflateView
import com.winsun.fruitmix.newdesign201804.file.offlineFile.data.OfflineFileDataSource
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.util.FileTool
import com.winsun.fruitmix.util.FileUtil
import com.winsun.fruitmix.util.SnackbarUtil
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel

import kotlinx.android.synthetic.main.offline_file_item.view.*
import java.io.File
import java.util.*

private const val OFFLINE_FILE_PRESENTER_TAG = "OfflineFilePresenter"

class OfflineFilePresenter(private val offlineFileDataSource: OfflineFileDataSource,
                           val loadingViewModel: LoadingViewModel, val noContentViewModel: NoContentViewModel,
                           val offlineFileView: OfflineFileView, val currentUserUUID: String) {

    private val offlineFileAdapter = OfflineFileAdapter()

    private val currentFileItems = mutableListOf<AbstractLocalFile>()

    private val rootFolderPath = FileUtil.getDownloadFileFolderPath(currentUserUUID)

    private val rootFilterFolderPath = listOf<String>(FileUtil.getAudioRecordFolderPath(), FileUtil.getLocalPhotoMiniThumbnailFolderPath(),
            FileUtil.getLocalPhotoThumbnailFolderPath(), FileUtil.getOriginalPhotoFolderPath(),
            ".download")

    private val retrieveFolderPath = mutableListOf<String>()

    fun initView(recyclerView: RecyclerView) {

        recyclerView.adapter = offlineFileAdapter

        enterFolder(rootFolderPath, rootFilterFolderPath)

    }

    private fun enterFolder(folderPath: String, filterFolderPath: List<String>) {

        loadingViewModel.showLoading.set(true)

        offlineFileDataSource.getFile(folderPath, filterFolderPath, object : BaseLoadDataCallback<AbstractLocalFile> {

            override fun onSucceed(data: MutableList<AbstractLocalFile>?, operationResult: OperationResult?) {

                retrieveFolderPath.add(folderPath)

                loadingViewModel.showLoading.set(false)

                if (data!!.isEmpty())
                    noContentViewModel.showNoContent.set(true)
                else {
                    noContentViewModel.showNoContent.set(false)

                    refreshData(data)
                }

                refreshTitle(folderPath)

            }

            override fun onFail(operationResult: OperationResult?) {

                retrieveFolderPath.add(folderPath)

                loadingViewModel.showLoading.set(false)
                noContentViewModel.showNoContent.set(true)

                refreshTitle(folderPath)
            }

        })

    }

    fun isRoot(): Boolean {
        return retrieveFolderPath.last() == rootFolderPath
    }

    fun gotoUpperLevel() {

        retrieveFolderPath.removeAt(retrieveFolderPath.lastIndex)

        val folderPath = retrieveFolderPath.last()

        val filterFolderPath = if (isRoot()) rootFilterFolderPath else Collections.emptyList()

        loadingViewModel.showLoading.set(true)

        offlineFileDataSource.getFile(folderPath, filterFolderPath, object : BaseLoadDataCallback<AbstractLocalFile> {

            override fun onFail(operationResult: OperationResult?) {
                loadingViewModel.showLoading.set(false)
                noContentViewModel.showNoContent.set(true)

                refreshTitle(folderPath)

            }

            override fun onSucceed(data: MutableList<AbstractLocalFile>?, operationResult: OperationResult?) {

                loadingViewModel.showLoading.set(false)

                if (data!!.isEmpty())
                    noContentViewModel.showNoContent.set(true)
                else {
                    noContentViewModel.showNoContent.set(false)

                    refreshData(data)
                }

                refreshTitle(folderPath)

            }

        })

    }

    private fun refreshTitle(folderPath: String) {

        if (folderPath == rootFolderPath)
            offlineFileView.setTitle(offlineFileView.getString(R.string.offline_file))
        else {

            val file = File(folderPath)
            offlineFileView.setTitle(file.name)

        }

    }


    private fun refreshData(data: MutableList<AbstractLocalFile>) {

        /*       val fileRecyclerViewAdapter = FileRecyclerViewAdapter({ file, position ->
               }, {

               }, { file, position ->

               }, sortPolicy = UserPreferenceContainer.userPreference.fileSortPolicy)

               fileRecyclerViewAdapter.fileViewMode = FileViewMode.LIST*/

        val folders = mutableListOf<AbstractLocalFile>()
        val files = mutableListOf<AbstractLocalFile>()

        data.forEach {

            if (it.isFolder)
                folders.add(it)
            else
                files.add(it)

        }

        currentFileItems.clear()

        currentFileItems.addAll(folders)
        currentFileItems.addAll(files)

        offlineFileAdapter.setItemList(currentFileItems)
        offlineFileAdapter.notifyDataSetChanged()

/*        val folderViewItems = mutableListOf<ViewItem>()
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
        fileRecyclerViewAdapter.notifyDataSetChanged()*/

    }

    private inner class OfflineFileAdapter : BaseRecyclerViewAdapter<SimpleViewHolder, AbstractLocalFile>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            val view = parent?.inflateView(R.layout.offline_file_item)

            return SimpleViewHolder(view)

        }

        override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

            val itemView = holder?.itemView

            val abstractLocalFile = mItemList[position]

            itemView?.fileTypeImageView?.setImageResource(abstractLocalFile.fileTypeResID)
            itemView?.fileFormatSizeTv?.text = FileUtil.formatFileSize(abstractLocalFile.size)
            itemView?.fileFormatTimeTv?.text = abstractLocalFile.dateText

            itemView?.folderNameTv?.text = abstractLocalFile.name

            itemView?.contentLayout?.setOnClickListener {

                if (abstractLocalFile.isFolder) {

                    enterFolder(abstractLocalFile.path, Collections.emptyList())

                } else {

                    FileUtil.openDownloadedFile(itemView.context, File(abstractLocalFile.path))

                }
            }

            itemView?.deleteLayout?.setOnClickListener {

                val result = if (abstractLocalFile.isFolder)
                    FileTool.getInstance().deleteDir(abstractLocalFile.path)
                else
                    FileTool.getInstance().deleteFile(abstractLocalFile.path)

                if (result) {

                    SnackbarUtil.showSnackBar(offlineFileView.getRootView(), Snackbar.LENGTH_SHORT,
                            messageStr = offlineFileView.getString(R.string.success, offlineFileView.getString(R.string.delete_file)))

                    currentFileItems.removeAt(position)
                    offlineFileAdapter.setItemList(currentFileItems)

                    offlineFileAdapter.notifyItemRemoved(holder.adapterPosition)

                } else {
                    SnackbarUtil.showSnackBar(offlineFileView.getRootView(), Snackbar.LENGTH_SHORT,
                            messageStr = offlineFileView.getString(R.string.fail, offlineFileView.getString(R.string.delete_file)))
                }

            }

        }

    }


}