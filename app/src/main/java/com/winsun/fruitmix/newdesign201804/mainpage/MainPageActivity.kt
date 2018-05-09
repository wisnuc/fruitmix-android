package com.winsun.fruitmix.newdesign201804.mainpage

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.file.list.FilePage
import kotlinx.android.synthetic.main.activity_main_page.*

class MainPageActivity : AppCompatActivity() {

    private lateinit var filePage: FilePage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        filePage = FilePage(this)

        mainPageFrameLayout.addView(filePage.view)

        filePage.refreshView()

    }

    override fun onBackPressed() {

        if (filePage.useDefaultBackPressFunction())
            super.onBackPressed()
        else
            filePage.onBackPressed()

    }

}
