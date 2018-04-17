package com.winsun.fruitmix.newdesign201804.equipment.abnormal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder

data class DiskItem(val brand: String, val capacity: Long)

class HandleAbnormalEquipmentPresenter {


    private inner class AbnormalDiskAdapter : BaseRecyclerViewAdapter<SimpleViewHolder, DiskItem>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            val view = LayoutInflater.from(parent?.context).inflate(R.layout.abnormal_disk_item, parent, false)

            return SimpleViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: SimpleViewHolder, position: Int) {


        }

    }


    private inner class AbnormalDiskInfoAdapter : BaseRecyclerViewAdapter<SimpleViewHolder, DiskItem>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            return SimpleViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.abnormal_disk_item_info, parent, false))

        }

        override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {


        }

    }


}