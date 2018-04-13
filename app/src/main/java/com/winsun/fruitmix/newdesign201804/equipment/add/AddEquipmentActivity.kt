package com.winsun.fruitmix.newdesign201804.equipment.add

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.command.AbstractCommand
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

        return View.inflate(this,R.layout.activity_add_equipment,root)

    }

    override fun getToolbarTitle(): String {
        return getString(R.string.add_equipment)
    }

    private fun showEquipmentMenu(){

        val bottomMenuItems = mutableListOf<BottomMenuItem>()

        bottomMenuItems.add(BottomMenuItem(0,getString(R.string.add_equipment_manually),object :AbstractCommand(){

            override fun execute() {
            }

            override fun unExecute() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }))

        bottomMenuItems.add(BottomMenuItem(0,getString(R.string.add_equipment_by_ip),object :AbstractCommand(){
            override fun execute() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun unExecute() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }))

    }


}
