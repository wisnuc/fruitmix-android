package com.winsun.fruitmix.newdesign201804.search

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.winsun.fruitmix.BaseActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.databinding.ActivitySearchBinding
import com.winsun.fruitmix.newdesign201804.file.list.data.InjectFileDataSource
import com.winsun.fruitmix.newdesign201804.file.operation.FileOperationView
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.InjectTransmissionTaskRepository
import com.winsun.fruitmix.newdesign201804.search.data.InjectSearchDataSource
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel
import com.winsun.fruitmix.viewmodel.ToolbarViewModel

const val SEARCH_PLACE = "search_place"

fun startSearchActivity(searchFolderUUIDs: ArrayList<String>, activity: Activity) {

    val intent = Intent(activity, SearchActivity::class.java)
    intent.putStringArrayListExtra(SEARCH_PLACE, searchFolderUUIDs)

    activity.startActivity(intent)

}

class SearchActivity : BaseActivity(), FileOperationView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activitySearchBinding: ActivitySearchBinding = DataBindingUtil.setContentView(this, R.layout.activity_search)

        val toolbarViewModel = ToolbarViewModel()

        toolbarViewModel.setBaseView(this)

        toolbarViewModel.menuResID.set(R.drawable.close)

        toolbarViewModel.titleTextColorResID.set(ContextCompat.getColor(this, R.color.eighty_seven_percent_black))

        activitySearchBinding.toolbarViewModel = toolbarViewModel

        val loadingViewModel = LoadingViewModel(this)

        activitySearchBinding.loadingViewModel = loadingViewModel

        val noContentViewModel = NoContentViewModel()

        noContentViewModel.noContentImgResId = R.drawable.search_not_found
        noContentViewModel.setNoContentText(getString(R.string.not_found))

        activitySearchBinding.noContentViewModel = noContentViewModel

        val searchPlaces = intent.getStringArrayListExtra(SEARCH_PLACE)

        val searchPresenter = SearchPresenter(
                activitySearchBinding, toolbarViewModel, loadingViewModel, noContentViewModel,
                InjectSearchDataSource.inject(this), ThreadManagerImpl.getInstance(),
                InjectTransmissionTaskRepository.provideInstance(this), InjectFileDataSource.inject(this),
                this, this,
                searchPlaces)

        searchPresenter.initView()

    }

    override fun getActivity(): Activity {
        return this
    }

}
