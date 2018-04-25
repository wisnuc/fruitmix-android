package com.winsun.fruitmix.newdesign201804.equipment.reinitialization.page

import android.content.Context
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.equipment.add.DiskMode
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.data.ReinitializationEquipmentDiskInfo
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.util.FileUtil
import kotlinx.android.synthetic.main.select_disk_item.view.*
import kotlinx.android.synthetic.main.select_disk_mode_viewpager_item.view.*

class SelectDiskModePage(val context: Context, private val reinitializationEquipmentDiskInfos: List<ReinitializationEquipmentDiskInfo>,
                         private val nextStep: (selectReinitializationEquipmentDiskInfos: List<ReinitializationEquipmentDiskInfo>,
                                                diskMode: DiskMode) -> Unit) : InitialPage {

    private val view = LayoutInflater.from(context).inflate(R.layout.select_disk_mode_viewpager_item, null)

    private var selectReinitializationEquipmentDiskInfos:MutableList<ReinitializationEquipmentDiskInfo> = mutableListOf()

    override fun getView(): View {
        return view
    }

    override fun refreshView() {

        selectReinitializationEquipmentDiskInfos.addAll(reinitializationEquipmentDiskInfos)

        view.selectModeRecyclerView.layoutManager = GridLayoutManager(context, 4)
        view.selectModeRecyclerView.itemAnimator = DefaultItemAnimator()

        val singleModeRadioButton = view.singleModeRadioBtn
        val raid1ModeRadioButton = view.raid1ModeRadioBtn

        var currentMode = DiskMode.SINGLE

        singleModeRadioButton.setOnClickListener {
            currentMode = DiskMode.SINGLE
        }

        raid1ModeRadioButton.setOnClickListener {
            currentMode = DiskMode.RAID1
        }

        val selectDiskItemRecyclerViewAdapter = SelectDiskItemRecyclerViewAdapter {

            raid1ModeRadioButton.isEnabled = it.size >= 2

            var availableDiskSize = 0.0
            var totalDiskSize = 0.0

            it.forEach {
                availableDiskSize += it.availableDiskSize
                totalDiskSize += it.totalDiskSize
            }

            if (currentMode == DiskMode.SINGLE) {
                view.available_capacity.text = FileUtil.formatFileSize(availableDiskSize)
                view.total_disk_capacity.text = FileUtil.formatFileSize(totalDiskSize)
            } else if (currentMode == DiskMode.RAID1) {
                view.available_capacity.text = FileUtil.formatFileSize(availableDiskSize)
                view.total_disk_capacity.text = FileUtil.formatFileSize(totalDiskSize)
            }

            selectReinitializationEquipmentDiskInfos = it

        }

        view.selectModeRecyclerView.adapter = selectDiskItemRecyclerViewAdapter

        selectDiskItemRecyclerViewAdapter.setItemList(reinitializationEquipmentDiskInfos)
        selectDiskItemRecyclerViewAdapter.notifyDataSetChanged()

        view.next_step.setOnClickListener {

            nextStep(selectReinitializationEquipmentDiskInfos,currentMode)
        }

    }

    private inner class SelectDiskItemRecyclerViewAdapter(val onSelectItemCountChanged: (selectItems: MutableList<ReinitializationEquipmentDiskInfo>) -> Unit)
        : BaseRecyclerViewAdapter<SimpleViewHolder, ReinitializationEquipmentDiskInfo>() {

        private var selectItems = mutableListOf<ReinitializationEquipmentDiskInfo>()

        override fun setItemList(itemList: List<ReinitializationEquipmentDiskInfo>?) {
            super.setItemList(itemList)

            selectItems.addAll(itemList!!)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            val view = LayoutInflater.from(context).inflate(R.layout.select_disk_item, parent, false)

            return SimpleViewHolder(view)

        }

        override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

            val reinitializationEquipmentDiskInfo = mItemList[position]

            holder?.itemView?.disk_item_tv?.text = reinitializationEquipmentDiskInfo.brand + "-" + FileUtil.formatFileSize(reinitializationEquipmentDiskInfo.totalDiskSize)

            holder?.itemView?.checkBox?.setOnClickListener {

                if (holder.itemView.checkBox.isChecked)
                    selectItems.remove(reinitializationEquipmentDiskInfo)
                else
                    selectItems.add(reinitializationEquipmentDiskInfo)

                onSelectItemCountChanged(selectItems)

            }

        }


    }


}