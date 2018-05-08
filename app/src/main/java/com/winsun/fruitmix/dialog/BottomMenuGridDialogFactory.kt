package com.winsun.fruitmix.dialog

import android.app.Dialog
import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.model.BottomMenuItem
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import kotlinx.android.synthetic.main.file_new_bottom_item.view.*

class BottomMenuGridDialogFactory(bottomMenuItems: List<BottomMenuItem>) : BaseBottomMenuDialogFactory(bottomMenuItems) {

    override fun createBottomSheetView(context: Context, bottomMenuItems: List<BottomMenuItem>): View {

        val bottomSheetView = View.inflate(context, R.layout.grid_bottom_menu_layout, null)

        val bottomSheetRecyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.recyclerView)

        val bottomMenuGridDialogRecyclerViewAdapter = BottomMenuGridDialogRecyclerViewAdapter()

        bottomSheetRecyclerView.adapter = bottomMenuGridDialogRecyclerViewAdapter

        bottomSheetRecyclerView.layoutManager = GridLayoutManager(context, 3)

        bottomMenuGridDialogRecyclerViewAdapter.setItemList(bottomMenuItems)
        bottomMenuGridDialogRecyclerViewAdapter.notifyDataSetChanged()

        return bottomSheetView

    }

    private class BottomMenuGridDialogRecyclerViewAdapter : BaseRecyclerViewAdapter<SimpleViewHolder, BottomMenuItem>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            val view = LayoutInflater.from(parent?.context).inflate(R.layout.file_new_bottom_item, parent, false)

            return SimpleViewHolder(view)

        }

        override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

            val view = holder?.itemView

            val bottomMenuItem = mItemList[position]

            view?.icon?.setImageResource(bottomMenuItem.iconResID)
            view?.text?.text = bottomMenuItem.text

            view?.setOnClickListener {
                bottomMenuItem.handleOnClickEvent()
            }

        }

    }


}