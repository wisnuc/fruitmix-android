package com.winsun.fruitmix.newdesign201804.equipment.abnormal

import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.winsun.fruitmix.R
import com.winsun.fruitmix.equipment.search.data.EquipmentDataSource
import com.winsun.fruitmix.newdesign201804.equipment.abnormal.data.DiskItemInfo
import com.winsun.fruitmix.newdesign201804.equipment.abnormal.data.DiskState
import com.winsun.fruitmix.newdesign201804.equipment.abnormal.strategy.HandleAbnormalEquipmentStrategyFactory
import com.winsun.fruitmix.newdesign201804.equipment.add.data.DiskMode
import com.winsun.fruitmix.newdesign201804.equipment.add.data.convertDiskMode
import com.winsun.fruitmix.newdesign201804.equipment.list.data.EquipmentItemDataSource
import com.winsun.fruitmix.newdesign201804.equipment.model.DiskAbnormalEquipmentItem
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.util.FileUtil
import kotlinx.android.synthetic.main.abnormal_disk_item.view.*
import kotlinx.android.synthetic.main.abnormal_disk_item_info.view.*
import kotlinx.android.synthetic.main.activity_handle_abnormal_equipment.view.*


class HandleAbnormalEquipmentPresenter(val itemUUID: String, val equipmentItemDataSource: EquipmentItemDataSource) {


    fun initView(rootView: View, context: Context) {

        val item = equipmentItemDataSource.getEquipmentItemInCache(itemUUID) as DiskAbnormalEquipmentItem

        rootView.diskModeTextView.text = convertDiskMode(item.diskMode, context)

        val diskItemInfos = item.diskItemInfos

        val abnormalDiskAdapter = AbnormalDiskAdapter()

        val abnormalDiskInfoAdapter = AbnormalDiskInfoAdapter()

        rootView.diskInfoRecyclerView.adapter = abnormalDiskAdapter
        rootView.lostDiskInfoRecyclerView.adapter = abnormalDiskInfoAdapter

        if (diskItemInfos.any { it.diskState == DiskState.NEW_AVAILABLE })
            abnormalDiskAdapter.setItemList(diskItemInfos.filter { it.diskState != DiskState.LOST })
        else
            abnormalDiskAdapter.setItemList(diskItemInfos)

        abnormalDiskAdapter.notifyDataSetChanged()

        abnormalDiskInfoAdapter.setItemList(diskItemInfos.filter { it.diskState == DiskState.LOST })
        abnormalDiskAdapter.notifyDataSetChanged()

        val handleAbnormalEquipmentStrategy = HandleAbnormalEquipmentStrategyFactory(item).generateStrategy()

        rootView.continueUseBtn.visibility = if (handleAbnormalEquipmentStrategy.canContinueUse()) View.VISIBLE else View.GONE
        rootView.repairBtn.visibility = if (handleAbnormalEquipmentStrategy.canRepair()) View.VISIBLE else View.GONE

        rootView.continueUseBtn.setOnClickListener { handleAbnormalEquipmentStrategy.onContinueUseClick(context) }
        rootView.repairBtn.setOnClickListener { handleAbnormalEquipmentStrategy.onRepairClick(context) }

    }

    fun handleShutdownBtnOnClick(view: View) {

        Snackbar.make(view, R.string.shutdown_hint, Snackbar.LENGTH_SHORT).show()

    }

    private inner class AbnormalDiskAdapter : BaseRecyclerViewAdapter<SimpleViewHolder, DiskItemInfo>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            val view = LayoutInflater.from(parent?.context).inflate(R.layout.abnormal_disk_item, parent, false)

            return SimpleViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: SimpleViewHolder, position: Int) {

            val diskItemInfo = mItemList[position]

            val view = viewHolder.itemView

            view.disk_state_icon.setImageResource(when (diskItemInfo.diskState) {
                DiskState.LOST -> R.drawable.lost_disk_green
                DiskState.NORMAL -> R.drawable.exist_disk_green
                DiskState.NEW_AVAILABLE -> R.drawable.disk_available_green
            })

            when (diskItemInfo.diskState) {
                DiskState.NEW_AVAILABLE -> view.addNewAvailableDiskIv.visibility = View.VISIBLE
                else -> view.addNewAvailableDiskIv.visibility = View.INVISIBLE
            }

            view.disk_info_tv.text = view.context.getString(R.string.disk_brand_space, diskItemInfo.brand, FileUtil.formatFileSize(diskItemInfo.totalSize))

        }

    }


    private inner class AbnormalDiskInfoAdapter : BaseRecyclerViewAdapter<SimpleViewHolder, DiskItemInfo>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            return SimpleViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.abnormal_disk_item_info, parent, false))

        }

        override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {

            val diskItemInfo = mItemList[position]

            val view = holder.itemView

            view.lostDiskDescriptionTextView.text = view.context.getString(R.string.lost_disk_description,
                    diskItemInfo.brand, FileUtil.formatFileSize(diskItemInfo.totalSize), diskItemInfo.serialNumber)

        }

    }


}