package com.winsun.fruitmix.newdesign201804.file.presenter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.winsun.fruitmix.BR
import com.winsun.fruitmix.databinding.FileFolderListItemBinding
import com.winsun.fruitmix.databinding.FileItemBinding
import com.winsun.fruitmix.databinding.FolderFileTitleBinding
import com.winsun.fruitmix.databinding.FolderItemBinding
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.newdesign201804.file.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.viewmodel.FileItemViewModel
import com.winsun.fruitmix.newdesign201804.file.viewmodel.FolderFileTitleViewModel
import com.winsun.fruitmix.newdesign201804.file.viewmodel.FolderItemViewModel
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.BindingViewHolder
import com.winsun.fruitmix.util.FileUtil

public class FilePresenter(val fileDataSource: FileDataSource) {

    private val fileRecyclerViewAdapter = FileRecyclerViewAdapter()




}

private const val ITEM_FOLDER_HEAD = 1
private const val ITEM_FOLDER = 2
private const val ITEM_FILE_HEAD = 3
private const val ITEM_FILE = 4

private const val ORIENTATION_LIST_TYPE = 0
private const val ORIENTATION_GRID_TYPE = 1

private class FileRecyclerViewAdapter : BaseRecyclerViewAdapter<BindingViewHolder, ViewItem>() {

    var currentOrientation = ORIENTATION_LIST_TYPE

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BindingViewHolder {

        val viewDataBinding = when (viewType) {

            ITEM_FOLDER_HEAD, ITEM_FILE_HEAD -> FolderFileTitleBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
            ITEM_FOLDER -> {

                if (currentOrientation == ORIENTATION_GRID_TYPE)
                    FolderItemBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
                else
                    FileFolderListItemBinding.inflate(LayoutInflater.from(parent?.context), parent, false)

            }
            ITEM_FILE -> {

                if (currentOrientation == ORIENTATION_GRID_TYPE)
                    FileItemBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
                else
                    FileFolderListItemBinding.inflate(LayoutInflater.from(parent?.context), parent, false)

            }
            else -> throw IllegalArgumentException("file view type is illegal")
        }

        return BindingViewHolder(viewDataBinding)

    }

    override fun onBindViewHolder(holder: BindingViewHolder?, position: Int) {

        val viewItem = mItemList[position]

        when (viewItem.type) {
            ITEM_FOLDER_HEAD, ITEM_FILE_HEAD -> {

                val itemFolderHead: ItemFolderHead = (viewItem as ItemFolderHead)

                val folderFileTitleBinding = holder?.viewDataBinding as FolderFileTitleBinding

                folderFileTitleBinding.folderFileTitleViewModel = itemFolderHead.folderFileTitleViewModel

            }
            ITEM_FILE -> {

                val itemFile = viewItem as ItemFile

                val fileItemViewModel = FileItemViewModel()
                fillgenerateFileItemViewModel(fileItemViewModel, itemFile.remoteFile)

                holder?.viewDataBinding?.setVariable(BR.fileItemViewModel, fileItemViewModel)

            }

            ITEM_FOLDER -> {

                val itemFolder = viewItem as ItemFolder

                val folderItemViewModel = FolderItemViewModel()
                fillgenerateFileItemViewModel(folderItemViewModel, itemFolder.remoteFolder)

                holder?.viewDataBinding?.setVariable(BR.fileItemViewModel, itemFolder)

            }

        }

        holder?.viewDataBinding?.executePendingBindings()


    }

    private fun fillgenerateFileItemViewModel(fileItemViewModel: FileItemViewModel, abstractRemoteFile: AbstractRemoteFile) {
        fileItemViewModel.fileTypeResID.set(abstractRemoteFile.fileTypeResID)
        fileItemViewModel.fileFormatSize.set(FileUtil.formatFileSize(abstractRemoteFile.size))
        fileItemViewModel.fileFormatTime.set(abstractRemoteFile.timeText)
        fileItemViewModel.folderName.set(abstractRemoteFile.name)
    }

    override fun getItemViewType(position: Int): Int {
        return mItemList[position].type
    }

}

private open class ItemFolderHead(val folderFileTitleViewModel: FolderFileTitleViewModel) : ViewItem {

    init {
        folderFileTitleViewModel.isFolder.set(true)
    }

    override fun getType(): Int {
        return ITEM_FOLDER_HEAD
    }

}

private class ItemFolder(val remoteFolder: RemoteFolder) : ViewItem {
    override fun getType(): Int {
        return ITEM_FOLDER
    }
}

private class ItemFileHead(folderFileTitleViewModel: FolderFileTitleViewModel) : ItemFolderHead(folderFileTitleViewModel) {

    init {
        folderFileTitleViewModel.isFolder.set(false)
    }

    override fun getType(): Int {
        return ITEM_FILE_HEAD
    }

}

private class ItemFile(val remoteFile: RemoteFile) : ViewItem {
    override fun getType(): Int {
        return ITEM_FILE
    }
}



