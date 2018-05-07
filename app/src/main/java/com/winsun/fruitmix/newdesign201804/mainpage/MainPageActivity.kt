package com.winsun.fruitmix.newdesign201804.mainpage

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.file.FilePage
import kotlinx.android.synthetic.main.activity_main_page.*

class MainPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        val filePage = FilePage(this)

        mainPageFrameLayout.addView(filePage.view)

        filePage.refreshView()

    }
}
