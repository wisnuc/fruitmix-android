package com.winsun.fruitmix.dialog

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.winsun.fruitmix.R
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.model.BottomMenuItem
import com.winsun.fruitmix.model.DivideBottomMenuItem
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import kotlinx.android.synthetic.main.file_menu_opeate_item.view.*


private const val BOTTOM_ITEM = 1
private const val BOTTOM_DIVIDE = 2

class FileMenuBottomDialogFactory(val abstractFile: AbstractFile, bottomMenuItems: List<BottomMenuItem>,
                                  val detailImageViewOnClick:(abstractFile: AbstractFile)->Unit) : BaseBottomMenuDialogFactory(bottomMenuItems) {

    override fun createBottomSheetView(context: Context, bottomMenuItems: List<BottomMenuItem>): View {

        val bottomSheetView = View.inflate(context, R.layout.file_menu_bottom_dialog, null)

        val fileTypeIv = bottomSheetView.findViewById<ImageView>(R.id.fileTypeIconIv)

        fileTypeIv.setImageResource(abstractFile.fileTypeResID)

        val fileNameTv = bottomSheetView.findViewById<TextView>(R.id.fileNameTextView)

        fileNameTv.text = abstractFile.name

        val detailIv = bottomSheetView.findViewById<ImageView>(R.id.detailImageView)

        detailIv.setOnClickListener {
            detailImageViewOnClick(abstractFile)
        }

        val bottomSheetRecyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.recyclerView)

        val bottomSheetRecyclerViewAdapter = FileMenuBottomDialogRecyclerViewAdapter()

        bottomSheetRecyclerView.adapter = bottomSheetRecyclerViewAdapter

        bottomSheetRecyclerView.layoutManager = LinearLayoutManager(context)

        bottomSheetRecyclerViewAdapter.setItemList(bottomMenuItems)
        bottomSheetRecyclerViewAdapter.notifyDataSetChanged()

        return bottomSheetView

    }

    private class FileMenuBottomDialogRecyclerViewAdapter : BaseRecyclerViewAdapter<SimpleViewHolder, BottomMenuItem>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            val view: View = if (viewType == BOTTOM_ITEM)
                LayoutInflater.from(parent?.context).inflate(R.layout
                        .file_menu_opeate_item, parent, false)
            else
                LayoutInflater.from(parent?.context).inflate(R.layout.file_menu_divide_item,
                        parent, false)

            return SimpleViewHolder(view)

        }

        override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

            val bottomMenuItem = mItemList[position]

            if (bottomMenuItem is DivideBottomMenuItem)
                return
            else {

                val view = holder?.itemView

                view?.operateImageView?.setImageResource(bottomMenuItem.iconResID)
                view?.operateTextView?.text = bottomMenuItem.text

                view?.setOnClickListener {
                    bottomMenuItem.handleOnClickEvent()
                }

                view?.switchBtn?.visibility = if (bottomMenuItem.isShowSwitchBtn) View.VISIBLE else View.INVISIBLE

            }

        }

        override fun getItemViewType(position: Int): Int {

            val bottomMenuItem = mItemList[position]

            return if (bottomMenuItem is DivideBottomMenuItem)
                BOTTOM_DIVIDE
            else
                BOTTOM_ITEM

        }

    }


}