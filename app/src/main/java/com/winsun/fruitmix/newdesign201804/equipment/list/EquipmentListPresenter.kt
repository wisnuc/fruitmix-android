package com.winsun.fruitmix.newdesign201804.equipment.list

import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.ActiveView
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.equipment.abnormal.HandleAbnormalEquipmentActivity
import com.winsun.fruitmix.newdesign201804.equipment.add.AddEquipmentActivity
import com.winsun.fruitmix.newdesign201804.equipment.list.data.EquipmentItemDataSource
import com.winsun.fruitmix.newdesign201804.login.LanLoginActivity
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewHolder
import kotlinx.android.synthetic.main.my_equipment_item.view.*

private const val EQUIPMENT_TYPE = 1
private const val ADD_EQUIPMENT_TYPE = 2

private const val TAG = "EquipmentListPresenter"

class EquipmentListPresenter(private val equipmentItemDataSource: EquipmentItemDataSource, val wechatUserGUID: String, val activeView: ActiveView) {

    private var equipmentListAdapter: EquipmentListAdapter = EquipmentListAdapter()

    private val equipmentItems: MutableList<ViewItem> = mutableListOf()

    fun refreshEquipment() {

        Log.d(TAG, "cache is dirty: " + equipmentItemDataSource.isCacheDirty())

        if (equipmentItemDataSource.isCacheDirty())
            equipmentItemDataSource.getEquipmentItems(object : BaseLoadDataCallbackWrapper<EquipmentItem>(

                    object : BaseLoadDataCallbackImpl<EquipmentItem>() {

                        override fun onSucceed(data: MutableList<EquipmentItem>?, operationResult: OperationResult?) {
                            super.onSucceed(data, operationResult)

                            equipmentItems.clear()

                            data?.forEach {
                                equipmentItems.add(EquipmentViewItem(it))
                            }

                            equipmentItems.add(AddEquipmentViewItem())

                            equipmentListAdapter.setItemList(equipmentItems)
                            equipmentListAdapter.notifyDataSetChanged()

                        }

                    }, activeView
            ) {})

    }

    fun getEquipmentListAdapter(): EquipmentListAdapter {
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

            val context = holder?.itemView?.context

            if (viewItem is EquipmentViewItem) {

                val equipmentItem = viewItem.equipmentItem

                holder?.itemView?.equipment_name?.text = equipmentItem.name

                holder?.itemView?.equipment_state_tv?.text = getEquipmentTypeStr(context!!, equipmentItem.equipment_TYPE)

                holder.itemView.equipment_state_icon.setImageResource(getEquipmentTypeIconId(equipmentItem.equipment_TYPE))

                holder.itemView.setOnClickListener {

                    when (equipmentItem.equipment_TYPE) {
                        EquipmentType.CLOUD_UNCONNECTED -> {
                            val intent = Intent(context, LanLoginActivity::class.java)
                            context.startActivity(intent)
                        }
                        EquipmentType.UNDER_REVIEW -> showMessageDialog(context, R.string.under_review,
                                context.getString(R.string.under_review_message, "Mark"))
                        EquipmentType.POWER_OFF -> showMessageDialog(context, R.string.equipment_already_power_off_title,
                                context.getString(R.string.equipment_already_power_off_message, "2018年4月11日17:40:10"))
                        EquipmentType.OFFLINE -> showMessageDialog(context, R.string.equipment_already_offline_title,
                                context.getString(R.string.equipment_already_offline_message))
                        EquipmentType.DISK_ABNORMAL -> {
                            context.startActivity(Intent(context, HandleAbnormalEquipmentActivity::class.java))
                        }
                        else -> {

                        }
                    }


                }

            } else if (viewItem is AddEquipmentViewItem) {

                holder?.itemView?.setOnClickListener {

                    context?.startActivity(Intent(context, AddEquipmentActivity::class.java))

                }

            }

        }

        override fun getItemViewType(position: Int): Int {
            return mItemList[position].type
        }

    }

    private fun showMessageDialog(context: Context, titleResID: Int, message: String) {

        AlertDialog.Builder(context).setTitle(titleResID)
                .setMessage(message)
                .setCancelable(true)
                .create().show()

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