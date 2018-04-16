package com.winsun.fruitmix.newdesign201804.equipment.reinitialization

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R

class ReinitializationActivity : BaseToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TODO()
    }

    override fun generateContent(root: ViewGroup?): View {

        return View.inflate(this,R.layout.activity_reinitialization,root)

    }

    override fun getToolbarTitle(): String {
        return getString(R.string.add_equipment)
    }

}
