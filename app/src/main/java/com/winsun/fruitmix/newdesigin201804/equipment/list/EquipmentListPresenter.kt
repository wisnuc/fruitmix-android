package com.winsun.fruitmix.newdesigin201804.equipment.list

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.newdesigin201804.login.LanLoginActivity
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewHolder
import kotlinx.android.synthetic.main.my_equipment_item.view.*

private const val EQUIPMENT_TYPE = 1
private const val ADD_EQUIPMENT_TYPE = 2


class EquipmentListPresenter {

    private var equipmentListAdapter: EquipmentListAdapter

    init {

        equipmentListAdapter = EquipmentListAdapter()

        val equipmentItems: MutableList<ViewItem> = mutableListOf()

        equipmentItems.add(EquipmentViewItem(EquipmentItem(EquipmentType.CLOUD_CONNECTED, "test1")))
        equipmentItems.add(EquipmentViewItem(EquipmentItem(EquipmentType.CLOUD_UNCONNECTED, "test2")))
        equipmentItems.add(EquipmentViewItem(EquipmentItem(EquipmentType.DISK_ABNORMAL, "test3")))
        equipmentItems.add(AddEquipmentViewItem())

        equipmentListAdapter.setItemList(equipmentItems)

    }

    public fun getEquipmentListAdapter(): EquipmentListAdapter {
        return equipmentListAdapter
    }

    inner class EquipmentListAdapter : BaseRecyclerViewAdapter<EquipmentListViewHolder, ViewItem>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): EquipmentListViewHolder {

            if (viewType == EQUIPMENT_TYPE) {
                if (parent != null) {

                    val view = LayoutInflater.from(parent.context).inflate(R.layout.my_equipment_item, parent, false)

                    return EquipmentListViewHolder(view)
                } else
                    throw IllegalArgumentException("equipment type parent is null")

            } else if (viewType == ADD_EQUIPMENT_TYPE) {

                if (parent != null) {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.add_equipment_item, parent, false)

                    return EquipmentListViewHolder(view)
                } else
                    throw IllegalArgumentException("add equipment type parent is null")

            } else
                throw UnsupportedOperationException("equipment view type not equipment type or add equipment type")


        }

        override fun onBindViewHolder(holder: EquipmentListViewHolder?, position: Int) {

            val viewItem = mItemList[position]

            if (viewItem is EquipmentViewItem) {

                val equipmentItem = viewItem.equipmentItem

                val context = holder?.itemView?.context

                holder?.itemView?.equipment_name?.text = equipmentItem.name

                holder?.itemView?.equipment_state_tv?.text = getEquipmentTypeStr(context!!, equipmentItem.equipment_TYPE)

                holder.itemView.equipment_state_icon.setImageResource(getEquipmentTypeIconId(equipmentItem.equipment_TYPE))

                holder.itemView.setOnClickListener {

                    val intent: Intent = when (equipmentItem.equipment_TYPE) {
                        EquipmentType.CLOUD_UNCONNECTED -> Intent(context, LanLoginActivity::class.java)
                        else -> Intent(context, LanLoginActivity::class.java)
                    }

                    context.startActivity(intent)

                }

            }

        }

        override fun getItemViewType(position: Int): Int {
            return mItemList[position].type
        }

    }

    inner class EquipmentListViewHolder(view: View) : BaseRecyclerViewHolder(view) {

        override fun refreshView(position: Int) {

        }

    }


    private class EquipmentViewItem(var equipmentItem: EquipmentItem) : ViewItem {

        override fun getType(): Int {

            return EQUIPMENT_TYPE
        }

    }

    private class AddEquipmentViewItem : ViewItem {
        override fun getType(): Int {
            return ADD_EQUIPMENT_TYPE
        }

    }


}