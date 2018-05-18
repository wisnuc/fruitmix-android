package com.winsun.fruitmix.newdesign201804.file.list

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.winsun.fruitmix.BaseActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.databinding.FilePageBinding
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository
import com.winsun.fruitmix.newdesign201804.file.list.data.InjectFileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.presenter.FilePresenter
import com.winsun.fruitmix.newdesign201804.file.offlineFile.OfflineFileActivity
import com.winsun.fruitmix.newdesign201804.file.sharedFolder.SharedFolderActivity
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.TransmissionTaskActivity
import com.winsun.fruitmix.newdesign201804.mainpage.DrawerItem
import com.winsun.fruitmix.newdesign201804.mainpage.MainPage
import com.winsun.fruitmix.util.Util
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel

class FilePage(val activity: BaseActivity) : MainPage {

    private val filePageBinding = FilePageBinding.inflate(LayoutInflater.from(activity), null, false)

    private val noContentViewModel = NoContentViewModel()
    private val loadingViewModel = LoadingViewModel(activity)

    private var filePresenter: FilePresenter

    private var drawerItems: MutableList<DrawerItem>

    private var initPage = true

    init {

        noContentViewModel.noContentImgResId = R.drawable.no_file
        noContentViewModel.setNoContentText(activity.getString(R.string.no_files))

        filePageBinding.loadingViewModel = loadingViewModel
        filePageBinding.noContentViewModel = noContentViewModel

        filePresenter = FilePresenter(InjectStationFileRepository.provideStationFileRepository(activity),
                noContentViewModel, loadingViewModel, filePageBinding,activity)

        val task = DrawerItem(R.drawable.transfer_menu_icon, activity.getString(R.string.transmission_task), {

            Util.startActivity(activity, TransmissionTaskActivity::class.java)

        })

        val shareToSharedFolder = DrawerItem(R.drawable.shared_folder, activity.getString(R.string.shared_folder), {

            Util.startActivity(activity, SharedFolderActivity::class.java)

        })

        val localFolder = DrawerItem(R.drawable.offline_available, activity.getString(R.string.offline_file), {

            Util.startActivity(activity, OfflineFileActivity::class.java)

        })

        val tag = DrawerItem(R.drawable.tag, activity.getString(R.string.tag), {})

        drawerItems = mutableListOf(
                task, shareToSharedFolder, localFolder, tag
        )
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

        if (initPage) {
            filePresenter.initView()

            initPage = false
        }

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

    override fun toggleOrientation() {
        filePresenter.toggleOrientation()
    }

    override fun handleMoreIvClick() {
        filePresenter.handleMoreIvClick()
    }

    override fun quitSelectMode() {
        filePresenter.quitSelectMode()
    }

    fun registerFilePageSelectActionListener(filePageSelectActionListener: FilePageSelectActionListener) {
        filePresenter.registerFilePageSelectActionListener(filePageSelectActionListener)
    }

    fun unregisterFilePageSelectActionListener(filePageSelectActionListener: FilePageSelectActionListener) {
        filePresenter.unregisterFilePageSelectActionListener(filePageSelectActionListener)
    }

    fun registerFilePageActionListener(filePageActionListener: FilePageActionListener) {
        filePresenter.registerFilePageActionListener(filePageActionListener)
    }

    fun unregisterFilePageActionListener(filePageActionListener: FilePageActionListener) {
        filePresenter.unregisterFilePageActionListener(filePageActionListener)
    }


}