package com.winsun.fruitmix.newdesign201804.file

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.winsun.fruitmix.BaseActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.databinding.FilePageBinding
import com.winsun.fruitmix.interfaces.Page
import com.winsun.fruitmix.newdesign201804.file.data.InjectFileDataSource
import com.winsun.fruitmix.newdesign201804.file.presenter.FilePresenter
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel

class FilePage(activity: Activity) :Page{

    private val filePageBinding = FilePageBinding.inflate(LayoutInflater.from(activity),null,false)

    private val noContentViewModel = NoContentViewModel()
    private val loadingViewModel = LoadingViewModel(activity)

    private var filePresenter:FilePresenter

    init {

        noContentViewModel.noContentImgResId = R.drawable.no_file
        noContentViewModel.setNoContentText(activity.getString(R.string.no_files))

        filePageBinding.loadingViewModel = loadingViewModel
        filePageBinding.noContentViewModel = noContentViewModel

        filePresenter = FilePresenter(InjectFileDataSource.inject(activity),
                noContentViewModel,loadingViewModel,filePageBinding)

    }

    fun generateView(){

        R.layout.file_page
        R.layout.search_file_card

        R.layout.folder_item
        R.layout.file_item

    }

    override fun getView(): View {
        return filePageBinding.root
    }

    override fun refreshView() {

        filePresenter.initView()

    }

    override fun refreshViewForce() {

    }

    override fun onDestroy() {

    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {

    }

    override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {

    }

    override fun canEnterSelectMode(): Boolean {
        return false
    }

}