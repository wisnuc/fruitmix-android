package com.winsun.fruitmix.newdesign201804.equipment.add

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.R

public class AddEquipmentPresenter {


}

public class EquipmentViewPager : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {



        return super.instantiateItem(container, position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

private fun showAvailableEquipmentDetail(context: Context) {

    AlertDialog.Builder(context)
            .setView(R.layout.available_equipment_detail)
            .create().show()

}

private fun showUnboundEquipmentDetail(context: Context) {

    val view = View.inflate(context, R.layout.unbound_equipment_detail_item, null)

    View.inflate(context,R.layout.equipment_list_item,null)

    AlertDialog.Builder(context)
            .setView(R.layout.unbound_equipment_detail)
            .create().show()
}


public class AddEquipmentByIpPresenter {


}
