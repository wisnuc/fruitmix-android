package com.winsun.fruitmix.newdesign201804.equipment.reinitialization.page

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.equipment.add.DiskMode
import com.winsun.fruitmix.newdesign201804.equipment.add.convertDiskMode
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.data.ReinitializationEquipmentDiskInfo
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.user.User
import com.winsun.fruitmix.util.FileUtil
import com.winsun.fruitmix.util.Util
import kotlinx.android.synthetic.main.reinitialization_equipment_item.view.*
import kotlinx.android.synthetic.main.reinitialization_succeed.view.*
import kotlinx.android.synthetic.main.unbound_equipment_detail.view.*

private data class ReinitializationEquipmentItem(val iconResID: Int, val itemExplain: String, val itemInfo: String)

class FinishReinitializationPage(val context: Context, val admin: User, private val selectEquipmentDiskInfos: List<ReinitializationEquipmentDiskInfo>,
                                 private val diskMode: DiskMode, private val gotIt:()->Unit) : InitialPage {

    private val view = LayoutInflater.from(context).inflate(R.layout.reinitialization_succeed, null)

    override fun getView(): View {
        return view
    }

    override fun refreshView() {

        view.got_it_btn.setOnClickListener {
            gotIt()
        }

        view.equipment_info_recyclerview.layoutManager = LinearLayoutManager(context)

        val reinitializationEquipmentItemAdapter = ReinitializationEquipmentItemAdapter()

        val reinitializationEquipmentItems = mutableListOf<ReinitializationEquipmentItem>()

        reinitializationEquipmentItems.add(ReinitializationEquipmentItem(R.drawable.user_icon_green,
                context.getString(R.string.admin), admin.userName))

        reinitializationEquipmentItems.add(ReinitializationEquipmentItem(0, context.getString(R.string.add_time),
                Util.formatDate(System.currentTimeMillis())))

        reinitializationEquipmentItems.add(ReinitializationEquipmentItem(R.drawable.disk_icon_green, context.getString(R.string.usage_mode),
                convertDiskMode(diskMode, context)))

        var availableDiskSize = 0.0
        var totalDiskSize = 0.0

        selectEquipmentDiskInfos.forEach {
            availableDiskSize += it.availableDiskSize
            totalDiskSize += it.totalDiskSize
        }

        reinitializationEquipmentItems.add(ReinitializationEquipmentItem(0, context.getString(R.string.total_capacity),
                FileUtil.formatFileSize(totalDiskSize)))

        reinitializationEquipmentItems.add(ReinitializationEquipmentItem(0, context.getString(R.string.available_capacity),
                FileUtil.formatFileSize(availableDiskSize)))

        reinitializationEquipmentItemAdapter.setItemList(reinitializationEquipmentItems)
        reinitializationEquipmentItemAdapter.notifyDataSetChanged()

    }

    private inner class ReinitializationEquipmentItemAdapter : BaseRecyclerViewAdapter<SimpleViewHolder, ReinitializationEquipmentItem>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            return SimpleViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.reinitialization_equipment_item, parent, false))

        }

        override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

            val reinitializationEquipmentItem = mItemList[position]

            if (reinitializationEquipmentItem.iconResID == 0)
                holder?.itemView?.itemIv?.visibility = View.INVISIBLE
            else {
                holder?.itemView?.itemIv?.visibility = View.VISIBLE
                holder?.itemView?.itemIv?.setImageResource(reinitializationEquipmentItem.iconResID)
            }

            holder?.itemView?.itemExplain?.text = reinitializationEquipmentItem.itemExplain
            holder?.itemView?.itemInfo?.text = reinitializationEquipmentItem.itemInfo

        }

    }

}