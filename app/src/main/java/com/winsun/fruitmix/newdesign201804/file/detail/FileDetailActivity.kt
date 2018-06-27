package com.winsun.fruitmix.newdesign201804.file.detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.newdesign201804.file.list.data.InjectFileDataSource
import com.winsun.fruitmix.user.datasource.InjectUser
import kotlinx.android.synthetic.main.activity_file_detail.*

const val FILE_UUID_KEY = "file_uuid_key"

class FileDetailActivity : BaseToolbarActivity() {

    companion object {

        fun start(selectFile: AbstractRemoteFile, context: Context) {

            FileDetailSelectFile.saveFile(selectFile)

            val intent = Intent(context, FileDetailActivity::class.java)

            context.startActivity(intent)

        }


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStatusBarToolbarBgColor(R.color.default_place_holder)

        val fileDetailPresenter = FileDetailPresenter(InjectFileDataSource.inject(this),
                InjectUser.provideRepository(this))

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
