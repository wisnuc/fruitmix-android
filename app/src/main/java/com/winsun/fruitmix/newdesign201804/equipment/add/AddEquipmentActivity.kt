package com.winsun.fruitmix.newdesign201804.equipment.add

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.command.BaseAbstractCommand
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory
import com.winsun.fruitmix.equipment.search.data.InjectEquipment
import com.winsun.fruitmix.model.BottomMenuItem
import com.winsun.fruitmix.newdesign201804.equipment.add.data.FakeNewEquipmentInfoDataSource
import kotlinx.android.synthetic.main.activity_add_equipment.*

class AddEquipmentActivity : BaseToolbarActivity(), SearchEquipmentUIState, EquipmentUIState {

    private lateinit var addEquipmentPresenter: AddEquipmentPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbarViewModel.showMenu.set(true)

        toolbarViewModel.setToolbarMenuBtnOnClickListener {

            showEquipmentMenu()

        }

        addEquipmentPresenter = AddEquipmentPresenter(InjectEquipment.provideEquipmentSearchManager(this),
                this, this, FakeNewEquipmentInfoDataSource())

        new_equipment_viewPager.adapter = addEquipmentPresenter.getViewPagerAdapter()

        viewpager_indicator.setViewPager(new_equipment_viewPager)

        new_equipment_viewPager.adapter?.registerDataSetObserver(viewpager_indicator.dataSetObserver)

        new_equipment_viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                addEquipmentPresenter.onPageSelect(position)

            }

        })


    }

    override fun generateContent(root: ViewGroup?): View {

        return LayoutInflater.from(this).inflate(R.layout.activity_add_equipment, root, false)

    }

    override fun getToolbarTitle(): String {
        return getString(R.string.add_equipment)
    }

    override fun startSearchState() {

        add_equipment_title.setText(R.string.searching_equipment)

        search_progressbar.visibility = View.VISIBLE

        refresh_layout.visibility = View.INVISIBLE

        new_equipment_viewPager.visibility = View.INVISIBLE

        operate_btn.visibility = View.INVISIBLE

    }

    override fun searchTimeoutState(showEquipmentViewPager: Boolean) {

        search_progressbar.visibility = View.INVISIBLE

        if (showEquipmentViewPager) {
            new_equipment_viewPager.visibility = View.VISIBLE
            refresh_layout.visibility = View.INVISIBLE

            operate_btn.visibility = View.VISIBLE

        } else {

            add_equipment_title.setText(R.string.undiscovered_equipment)

            new_equipment_viewPager.visibility = View.INVISIBLE
            refresh_layout.visibility = View.VISIBLE

            operate_btn.visibility = View.INVISIBLE
        }

    }

    override fun searchSucceedState() {

        searchTimeoutState(true)

    }

    override fun useExistDiskData() {

        operate_btn.setBackgroundResource(R.drawable.green_btn_bg)

        operate_btn.setText(R.string.use_exist_disk_data)

    }

    override fun selectDiskBeforeUseExistDiskData() {

        operate_btn.setBackgroundResource(R.drawable.white_btn_bg)

        operate_btn.setText(R.string.use_exist_disk_data)

    }

    override fun addAvailableEquipment() {

        operate_btn.setBackgroundResource(R.drawable.green_btn_bg)

        operate_btn.setText(R.string.add_immediately)

    }

    override fun reinitialization() {

        operate_btn.setBackgroundResource(R.drawable.green_btn_bg)

        operate_btn.setText(R.string.next_step)

    }

    override fun refreshStationName(stationName: String) {
        add_equipment_title.text = stationName
    }

    private fun showEquipmentMenu() {

        val bottomMenuItems = mutableListOf<BottomMenuItem>()

        if (addEquipmentPresenter.getItemSize() > 0) {

            bottomMenuItems.add(BottomMenuItem(0, getString(R.string.refresh), object : BaseAbstractCommand() {

                override fun execute() {
                    super.execute()

                    addEquipmentPresenter.startSearchState()
                }

            }))

        }

        bottomMenuItems.add(BottomMenuItem(0, getString(R.string.add_equipment_manually), object : BaseAbstractCommand() {

            override fun execute() {

            }

        }))

        bottomMenuItems.add(BottomMenuItem(0, getString(R.string.add_equipment_by_ip), object : BaseAbstractCommand() {
            override fun execute() {
                enterAddEquipmentByIp()
            }

        }))

        BottomMenuDialogFactory(bottomMenuItems).createDialog(this).show()

    }

    private fun enterAddEquipmentByIp() {
        startActivity(Intent(this, AddEquipmentByIpActivity::class.java))
    }

}
