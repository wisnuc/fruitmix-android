package com.winsun.fruitmix.newdesign201804.file.upload

import android.app.Activity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.file.view.LocalFileFragment
import com.winsun.fruitmix.newdesign201804.util.inflateView
import com.winsun.fruitmix.newdesign201804.file.move.SelectMoveFileDataSource
import kotlinx.android.synthetic.main.activity_file_browser.*

class FileBrowserActivity : BaseToolbarActivity() {

    private lateinit var localFileFragment: LocalFileFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        localFileFragment = LocalFileFragment(this)

        frameLayout.addView(localFileFragment.view)

        localFileFragment.setSelectMode(true)
        localFileFragment.refreshView()

        toolbarViewModel.showSelect.set(true)

        toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(this, R.color.eighty_seven_percent_black))

        toolbarViewModel.setToolbarSelectBtnOnClickListener {

            SelectMoveFileDataSource.saveSelectFiles(localFileFragment.selectFiles)

            setResult(Activity.RESULT_OK)
            finish()

        }


    }

    override fun generateContent(root: ViewGroup?): View {
        return root!!.inflateView(R.layout.activity_file_browser)
    }

    override fun getToolbarTitle(): String {
        return "选择文件"
    }

    override fun onBackPressed() {
        if (!localFileFragment.onBackPressed())
            super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()

        localFileFragment.onDestroy()
    }

}
