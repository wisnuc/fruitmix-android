package com.winsun.fruitmix.newdesign201804.file.detail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.winsun.fruitmix.R
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.newdesign201804.component.inflateView
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.util.FileUtil

import kotlinx.android.synthetic.main.folder_detail_item.view.*

class FileDetailPresenter(val fileDataSource: FileDataSource, val fileUUID: String) {

    fun initView(fileNameTextView: TextView, fileTypeImageView: ImageView, recyclerView: RecyclerView) {

        val abstractFile = fileDataSource.getFileByUUID(fileUUID)

        fileNameTextView.text = abstractFile?.name
        fileTypeImageView.setImageResource(abstractFile?.fileTypeResID!!)

        initRecyclerView(abstractFile, recyclerView)

    }

    private fun initRecyclerView(abstractFile: AbstractFile, recyclerView: RecyclerView) {

        val context = recyclerView.context

        val fileDetailItemViewModels = mutableListOf<FileDetailItemViewModel>()

        fileDetailItemViewModels.add(FileDetailItemViewModel(context.getString(R.string.key_type), "Power Point", 0))
        fileDetailItemViewModels.add(FileDetailItemViewModel(context.getString(R.string.key_size),
                FileUtil.formatFileSize(abstractFile.size), 0))

        fileDetailItemViewModels.add(FileDetailItemViewModel(context.getString(R.string.key_position),
                "My Drive", R.drawable.ic_folder))

        if (abstractFile.isFolder) {

            fileDetailItemViewModels.add(FileDetailItemViewModel(context.getString(R.string.key_file_quantity),
                    "100", 0))

            fileDetailItemViewModels.add(FileDetailItemViewModel(context.getString(R.string.key_media_quantity),
                    "40", 0))
        }

        fileDetailItemViewModels.add(FileDetailItemViewModel(context.getString(R.string.key_create),
                abstractFile.timeText, 0))

        fileDetailItemViewModels.add(FileDetailItemViewModel(context.getString(R.string.key_modify),
                abstractFile.timeText, 0))

        val fileDetailAdapter = FileDetailAdapter()

        recyclerView.adapter = fileDetailAdapter

        fileDetailAdapter.setItemList(fileDetailItemViewModels)
        fileDetailAdapter.notifyDataSetChanged()

    }

}


class FileDetailAdapter : BaseRecyclerViewAdapter<SimpleViewHolder, FileDetailItemViewModel>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

        val view = parent?.inflateView(R.layout.folder_detail_item)

        return SimpleViewHolder(view)

    }

    override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

        val view = holder?.itemView

        val fileDetailItemViewModel = mItemList[position]

        view?.keyTextView?.text = fileDetailItemViewModel.key
        if (fileDetailItemViewModel.valueResID == 0) {
            view?.valueImageView?.visibility = View.GONE
        } else {
            view?.valueImageView?.visibility = View.VISIBLE
            view?.valueImageView?.setImageResource(fileDetailItemViewModel.valueResID)
        }

        view?.valueTextView?.text = fileDetailItemViewModel.textValue

    }

}

data class FileDetailItemViewModel(val key: String, val textValue: String, val valueResID: Int)
