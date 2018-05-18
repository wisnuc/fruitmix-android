package com.winsun.fruitmix.newdesign201804.file.sharedFolder

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository
import com.winsun.fruitmix.newdesign201804.file.list.data.InjectFileDataSource
import kotlinx.android.synthetic.main.activity_shared_folder.*

class SharedFolderActivity : BaseToolbarActivity(),SharedFolderView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbarViewModel.showMenu.set(true)
        toolbarViewModel.menuResID.set(R.drawable.search_black)

        toolbarViewModel.setToolbarMenuBtnOnClickListener {

        }

        val sharedFolderPresenter = SharedFolderPresenter(
                InjectStationFileRepository.provideStationFileRepository(this),this)

        recyclerView.layoutManager = LinearLayoutManager(this)

        sharedFolderPresenter.initView(recyclerView)

    }

    override fun generateContent(root: ViewGroup?): View {
        return LayoutInflater.from(this).inflate(R.layout.activity_shared_folder, root, false)
    }

    override fun getToolbarTitle(): String {
        return getString(R.string.shared_folder)
    }

    override fun getContext(): Context {
        return this
    }

}
