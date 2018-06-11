package com.winsun.fruitmix.newdesign201804.file.offlineFile

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.databinding.ActivityOfflineFileBinding
import com.winsun.fruitmix.newdesign201804.file.offlineFile.data.InjectOfflineFileDataSource
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel
import kotlinx.android.synthetic.main.activity_offline_file.*

class OfflineFileActivity : BaseToolbarActivity() {

    private lateinit var activitiyOfflineFileBinding: ActivityOfflineFileBinding

    private val loadingViewModel = LoadingViewModel(this)

    private val noContentViewModel = NoContentViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activitiyOfflineFileBinding.loadingViewModel = loadingViewModel

        noContentViewModel.noContentImgResId = R.drawable.no_file
        noContentViewModel.setNoContentText(getString(R.string.no_files))

        activitiyOfflineFileBinding.noContentViewModel = noContentViewModel

        val offlineFilePresenter = OfflineFilePresenter(InjectOfflineFileDataSource.inject(this),
                loadingViewModel,noContentViewModel)

        recyclerView.layoutManager = LinearLayoutManager(this)

        offlineFilePresenter.initView(recyclerView)

    }

    override fun generateContent(root: ViewGroup?): View {

        activitiyOfflineFileBinding = ActivityOfflineFileBinding.inflate(LayoutInflater.from(this),
                root, false)

        return activitiyOfflineFileBinding.root
    }

    override fun getToolbarTitle(): String {
        return getString(R.string.offline_file)
    }

}
