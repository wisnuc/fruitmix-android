package com.winsun.fruitmix.newdesign201804.equipment.add

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.command.AbstractCommand
import com.winsun.fruitmix.command.BaseAbstractCommand
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory
import com.winsun.fruitmix.model.BottomMenuItem

class AddEquipmentActivity : BaseToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbarViewModel.showMenu.set(true)

        toolbarViewModel.setToolbarMenuBtnOnClickListener {

            showEquipmentMenu()

        }

    }

    override fun generateContent(root: ViewGroup?): View {

        return LayoutInflater.from(this).inflate(R.layout.activity_add_equipment, root, false)

    }

    override fun getToolbarTitle(): String {
        return getString(R.string.add_equipment)
    }

    private fun showEquipmentMenu() {

        val bottomMenuItems = mutableListOf<BottomMenuItem>()

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
