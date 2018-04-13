package com.winsun.fruitmix.newdesign201804.equipment.list

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import com.winsun.fruitmix.R
import kotlinx.android.synthetic.main.activity_equipment_list.*

class EquipmentListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipment_list)

        equipmentRecyclerView.layoutManager = GridLayoutManager(this, 2)
        equipmentRecyclerView.itemAnimator = DefaultItemAnimator()

        val equipmentListPresenter = EquipmentListPresenter()

        val equipmentListAdapter = equipmentListPresenter.getEquipmentListAdapter()

        equipmentRecyclerView.adapter = equipmentListAdapter

        equipmentListAdapter.notifyDataSetChanged()

    }


}
