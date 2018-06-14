package com.winsun.fruitmix.newdesign201804.search

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseActivity
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.databinding.ActivitySearchBinding
import com.winsun.fruitmix.newdesign201804.file.list.data.InjectFileDataSource
import com.winsun.fruitmix.newdesign201804.search.data.InjectSearchDataSource
import com.winsun.fruitmix.util.Util
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel
import com.winsun.fruitmix.viewmodel.ToolbarViewModel
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.searchpage_toolbar_layout.view.*

const val SEARCH_PLACE = "seach_place"

fun startSearchActivity(searchFolderUUID: String, activity: Activity) {

    val intent = Intent(activity, SearchActivity::class.java)
    intent.putExtra(SEARCH_PLACE, searchFolderUUID)

    activity.startActivity(intent)

}

class SearchActivity : BaseActivity() {

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

        val searchPlace = intent.getStringExtra(SEARCH_PLACE)

        val searchPresenter = SearchPresenter(
                activitySearchBinding, toolbarViewModel, loadingViewModel, noContentViewModel,
                InjectSearchDataSource.inject(this), searchPlace)


        searchPresenter.initView()

    }


}
