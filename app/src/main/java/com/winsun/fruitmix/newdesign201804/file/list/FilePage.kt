package com.winsun.fruitmix.newdesign201804.file.list

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.winsun.fruitmix.R
import com.winsun.fruitmix.databinding.FilePageBinding
import com.winsun.fruitmix.interfaces.Page
import com.winsun.fruitmix.mainpage.MainPagePresenterImpl
import com.winsun.fruitmix.newdesign201804.file.list.data.InjectFileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.presenter.FilePresenter
import com.winsun.fruitmix.newdesign201804.file.offlineFile.OfflineFileActivity
import com.winsun.fruitmix.newdesign201804.file.sharedFolder.SharedFolderActivity
import com.winsun.fruitmix.newdesign201804.mainpage.DrawerItem
import com.winsun.fruitmix.newdesign201804.mainpage.MainPage
import com.winsun.fruitmix.newdesign201804.search.SearchActivity
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel
import kotlin.math.acos

class FilePage(val activity: Activity) : MainPage {

    private val filePageBinding = FilePageBinding.inflate(LayoutInflater.from(activity), null, false)

    private val noContentViewModel = NoContentViewModel()
    private val loadingViewModel = LoadingViewModel(activity)

    private var filePresenter: FilePresenter

    private var drawerItems: MutableList<DrawerItem>

    init {

        noContentViewModel.noContentImgResId = R.drawable.no_file
        noContentViewModel.setNoContentText(activity.getString(R.string.no_files))

        filePageBinding.loadingViewModel = loadingViewModel
        filePageBinding.noContentViewModel = noContentViewModel

        filePresenter = FilePresenter(InjectFileDataSource.inject(activity),
                noContentViewModel, loadingViewModel, filePageBinding)

        val task = DrawerItem(R.drawable.transfer_menu_icon, activity.getString(R.string.transmission_task), {})

        val shareToSharedFolder = DrawerItem(R.drawable.shared_folder, activity.getString(R.string.shared_folder), {

            val intent = Intent(activity, SharedFolderActivity::class.java)
            activity.startActivity(intent)

        })

        val localFolder = DrawerItem(R.drawable.offline_available, activity.getString(R.string.offline_file), {

            val intent = Intent(activity, OfflineFileActivity::class.java)
            activity.startActivity(intent)

        })

        val tag = DrawerItem(R.drawable.tag, activity.getString(R.string.tag), {})

        drawerItems = mutableListOf(
                task, shareToSharedFolder, localFolder, tag
        )

        val searchTextView = filePageBinding.searchFileCard?.findViewById<TextView>(R.id.searchTextView)

        searchTextView?.setOnClickListener {

            val intent = Intent(activity, SearchActivity::class.java)
            activity.startActivity(intent)

        }

    }

    fun generateView() {

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

    override fun useDefaultBackPressFunction(): Boolean {
        return filePresenter.useDefaultBackPressFunction()
    }

    override fun onBackPressed() {
        filePresenter.onBackPressed()
    }

    override fun getDrawerTitle(): String {
        return activity.getString(R.string.drawer_file_title)
    }

    override fun getDrawerItems(): List<DrawerItem> {
        return drawerItems
    }


}