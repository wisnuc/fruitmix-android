package com.winsun.fruitmix.newdesign201804.file.sharedFolder

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl
import com.winsun.fruitmix.databinding.ActivitySharedFolderBinding
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.component.getCurrentUserHome
import com.winsun.fruitmix.newdesign201804.component.getCurrentUserUUID
import com.winsun.fruitmix.newdesign201804.file.list.data.InjectFileDataSource
import com.winsun.fruitmix.newdesign201804.file.sharedFolder.data.InjectSharedFolderDataSource
import com.winsun.fruitmix.newdesign201804.file.sharedFolder.data.SharedFolderDataSource
import com.winsun.fruitmix.newdesign201804.search.SearchActivity
import com.winsun.fruitmix.newdesign201804.search.startSearchActivity
import com.winsun.fruitmix.user.User
import com.winsun.fruitmix.user.datasource.InjectUser
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel
import kotlinx.android.synthetic.main.activity_shared_folder.*

class SharedFolderActivity : BaseToolbarActivity(), SharedFolderView {

    private lateinit var sharedFolderPresenter: SharedFolderPresenter

    private lateinit var sharedFolderDataSource: SharedFolderDataSource

    private lateinit var binding: ActivitySharedFolderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val noContentViewModel = NoContentViewModel()
        noContentViewModel.noContentImgResId = R.drawable.no_file
        noContentViewModel.setNoContentText(getString(R.string.no_files))

        binding.noContentViewModel = noContentViewModel

        val loadingViewModel = LoadingViewModel(this)

        binding.loadingViewModel = loadingViewModel

        toolbarViewModel.showMenu.set(true)
        toolbarViewModel.menuResID.set(R.drawable.search_black)

        toolbarViewModel.setToolbarMenuBtnOnClickListener {

            startSearchActivity(getCurrentUserHome(), this)

        }

        sharedFolderDataSource = InjectSharedFolderDataSource.inject(this)

        sharedFolderPresenter = SharedFolderPresenter(
                InjectFileDataSource.inject(this), this,
                loadingViewModel, noContentViewModel,
                sharedFolderDataSource)

        recyclerView.layoutManager = LinearLayoutManager(this)

        sharedFolderPresenter.initView(recyclerView)

        addFab.setOnClickListener {
            sharedFolderPresenter.showAddSharedFolder()
        }

    }


    override fun generateContent(root: ViewGroup?): View {

        binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_shared_folder, null, false)

        return binding.root

    }

    override fun getToolbarTitle(): String {
        return getString(R.string.shared_folder)
    }

    override fun getContext(): Context {
        return this
    }

    override fun getRootView(): View {
        return rootLayout
    }

    override fun setAddFabVisibility(visibility: Int) {
        addFab.visibility = visibility
    }

    override fun onBackPressed() {

        if (sharedFolderPresenter.notRoot())
            sharedFolderPresenter.onBackPressed()
        else
            super.onBackPressed()

    }

}
