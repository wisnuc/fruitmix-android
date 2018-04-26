package com.winsun.fruitmix.newdesign201804.equipment.abnormal

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.equipment.add.data.DiskMode
import com.winsun.fruitmix.newdesign201804.equipment.add.data.convertDiskMode
import com.winsun.fruitmix.newdesign201804.equipment.list.data.InjectEquipmentItemDataSource
import kotlinx.android.synthetic.main.activity_handle_abnormal_equipment.*

const val EQUIPMENT_ITEM_UUID_KEY = "equipment_item_uuid_key"

class HandleAbnormalEquipmentActivity : BaseToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStatusBarToolbarBgColor(R.color.new_design_primary_color)
        setToolbarWhiteStyle(toolbarViewModel)

        toolbarViewModel.navigationIconResId.set(R.drawable.white_clear)
        toolbarViewModel.showSelect.set(true)
        toolbarViewModel.selectTextResID.set(R.string.shutdown)

        val itemUUID = intent.getStringExtra(EQUIPMENT_ITEM_UUID_KEY)

        val handleAbnormalEquipmentPresenter = HandleAbnormalEquipmentPresenter(itemUUID,InjectEquipmentItemDataSource.inject(this))

        diskInfoRecyclerView.layoutManager = GridLayoutManager(this,4)
        diskInfoRecyclerView.itemAnimator = DefaultItemAnimator()

        lostDiskInfoRecyclerView.layoutManager = LinearLayoutManager(this)
        lostDiskInfoRecyclerView.itemAnimator = DefaultItemAnimator()

        handleAbnormalEquipmentPresenter.initView(
                diskInfoRecyclerView,lostDiskInfoRecyclerView,diskModeTextView,this
        )

    }

    override fun generateContent(root: ViewGroup?): View {

        return LayoutInflater.from(this).inflate(R.layout.activity_handle_abnormal_equipment,root,false)

    }

    override fun getToolbarTitle(): String {
        return getString(R.string.disk_loss)
    }

}
