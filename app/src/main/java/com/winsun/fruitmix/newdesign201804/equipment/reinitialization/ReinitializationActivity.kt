package com.winsun.fruitmix.newdesign201804.equipment.reinitialization

import android.os.Bundle
import android.view.LayoutInflater
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

        View.inflate(this,R.layout.reinitialization_succeed,null)

        return LayoutInflater.from(this).inflate(R.layout.activity_reinitialization,root,false)

    }

    override fun getToolbarTitle(): String {
        return getString(R.string.add_equipment)
    }

}
