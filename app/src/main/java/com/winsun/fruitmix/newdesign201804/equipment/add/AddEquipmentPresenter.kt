package com.winsun.fruitmix.newdesign201804.equipment.add

import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.equipment.search.data.Equipment
import com.winsun.fruitmix.equipment.search.data.EquipmentSearchManager

interface AddEquipmentUIState{

    fun startSearchState()
    fun searchTimeoutState()
    fun searchSucceedState()

}

private const val SEARCH_TIMEOUT = 0x1001
private const val SEARCH_SUCCEED = 0x1002

private const val SEARCH_TIMEOUT_SECOND = 6 * 1000L

public class AddEquipmentPresenter(private val equipmentSearchManager: EquipmentSearchManager,
                                   private val addEquipmentUIState: AddEquipmentUIState):AddEquipmentUIState {

    private val equipments:MutableList<Equipment> = mutableListOf()
    private val equipmentViewPager = EquipmentViewPager(equipments)

    private val customHandler = CustomHandler(this)

    override fun startSearchState() {

        equipmentSearchManager.startDiscovery {

            customHandler.removeMessages(SEARCH_TIMEOUT)
            customHandler.sendEmptyMessage(SEARCH_SUCCEED)

            equipments.add(it)

            equipmentViewPager.notifyDataSetChanged()

        }

        customHandler.sendEmptyMessageDelayed(SEARCH_TIMEOUT, SEARCH_TIMEOUT_SECOND)
    }

    override fun searchTimeoutState() {

        equipmentSearchManager.stopDiscovery()

        addEquipmentUIState.searchTimeoutState()
    }

    override fun searchSucceedState() {

        addEquipmentUIState.searchSucceedState()

    }

}

private  class CustomHandler(val addEquipmentPresenter: AddEquipmentPresenter):Handler(){

    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)

        when(msg?.what){
            SEARCH_SUCCEED->addEquipmentPresenter.searchSucceedState()
            SEARCH_TIMEOUT->addEquipmentPresenter.searchTimeoutState()
        }

    }

}

private class EquipmentViewPager(private val equipments:MutableList<Equipment>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val view = View.inflate(container.context, R.layout.equipment_list_item, null)

        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return equipments.size
    }

}

private fun showAvailableEquipmentDetail(context: Context) {

    AlertDialog.Builder(context)
            .setView(R.layout.available_equipment_detail)
            .create().show()

}

private fun showUnboundEquipmentDetail(context: Context) {

    val view = View.inflate(context, R.layout.unbound_equipment_detail_item, null)


    AlertDialog.Builder(context)
            .setView(R.layout.unbound_equipment_detail)
            .create().show()
}


public class AddEquipmentByIpPresenter {


}
