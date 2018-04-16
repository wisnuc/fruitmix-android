package com.winsun.fruitmix.newdesign201804.equipment.abnormal

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R

class HandleAbnormalEquipmentActivity : BaseToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbarViewModel.navigationIconResId.set(R.drawable.red_clear)
        toolbarViewModel.showSelect.set(true)
        toolbarViewModel.selectTextResID.set(R.string.shutdown)

    }

    override fun generateContent(root: ViewGroup?): View {

        return View.inflate(this,R.layout.activity_handle_abnormal_equipment,root)
    }

    override fun getToolbarTitle(): String {
        return getString(R.string.disk_loss)
    }

}
