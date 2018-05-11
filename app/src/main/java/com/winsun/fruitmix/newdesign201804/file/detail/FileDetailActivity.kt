package com.winsun.fruitmix.newdesign201804.file.detail

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.newdesign201804.file.list.data.InjectFileDataSource
import kotlinx.android.synthetic.main.activity_file_detail.*

const val FILE_UUID_KEY = "file_uuid_key"

class FileDetailActivity : BaseToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fileUUID = intent.getStringExtra(FILE_UUID_KEY)

        val fileDetailPresenter = FileDetailPresenter(InjectFileDataSource.inject(this), fileUUID)

        recyclerView.layoutManager = LinearLayoutManager(this)

        fileDetailPresenter.initView(fileNameTextView, fileTypeImageView, recyclerView)

    }

    override fun generateContent(root: ViewGroup?): View {
        return LayoutInflater.from(root?.context).inflate(R.layout.activity_file_detail, root, false)
    }

    override fun getToolbarTitle(): String {
        return ""
    }

}
