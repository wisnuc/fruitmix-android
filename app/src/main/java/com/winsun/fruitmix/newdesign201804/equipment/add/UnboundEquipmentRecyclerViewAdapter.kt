package com.winsun.fruitmix.newdesign201804.equipment.add

import android.view.LayoutInflater
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.util.FileUtil
import kotlinx.android.synthetic.main.unbound_equipment_detail_item.view.*


class UnboundEquipmentRecyclerViewAdapter : BaseRecyclerViewAdapter<SimpleViewHolder, UnboundEquipmentDiskInfo>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.unbound_equipment_detail_item, parent, false)

        return SimpleViewHolder(itemView)

    }


    override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

        val unboundEquipmentDiskInfo = mItemList[position]

        val view = holder?.itemView

        val context = view?.context

        view?.original_equipment_name?.text = unboundEquipmentDiskInfo.originalEquipmentName

        view?.disk_mode?.text = when (unboundEquipmentDiskInfo.diskMode) {
            DiskMode.SINGLE -> context?.getString(R.string.single)
            DiskMode.RAID1 -> context?.getString(R.string.raid1)
        }

        view?.disk_num?.text = "$position/$itemCount"

        view?.disk_space?.text = "${FileUtil.formatFileSize(unboundEquipmentDiskInfo.availableDiskSize)}/${FileUtil.formatFileSize(unboundEquipmentDiskInfo.totalDiskSize)}"

        if (position == 0)
            view?.select_volume_radioButton?.isChecked = true
        

    }

}
