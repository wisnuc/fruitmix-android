package com.winsun.fruitmix.newdesign201804.file.offlineFile

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.file.offlineFile.data.InjectOfflineFileDataSource
import kotlinx.android.synthetic.main.activity_offline_file.*

class OfflineFileActivity : BaseToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val offlineFilePresenter = OfflineFilePresenter(InjectOfflineFileDataSource.inject(this))

        recyclerView.layoutManager = LinearLayoutManager(this)

        offlineFilePresenter.initView(recyclerView)

    }

    override fun generateContent(root: ViewGroup?): View {
        return LayoutInflater.from(this).inflate(R.layout.activity_offline_file, root, false)
    }

    override fun getToolbarTitle(): String {
        return getString(R.string.offline_file)
    }

}
