package com.winsun.fruitmix.newdesign201804.file.list

import android.app.Activity
import android.content.Intent
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import com.winsun.fruitmix.BaseActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.databinding.FilePageBinding
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository
import com.winsun.fruitmix.newdesign201804.component.getCurrentUserUUID
import com.winsun.fruitmix.newdesign201804.file.list.data.InjectFileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.presenter.FilePresenter
import com.winsun.fruitmix.newdesign201804.file.move.FILE_COPY_REQUEST_CODE
import com.winsun.fruitmix.newdesign201804.file.move.FILE_MOVE_REQUEST_CODE
import com.winsun.fruitmix.newdesign201804.file.move.FILE_SHARE_TO_SHARED_FOLDER_REQUEST_CODE
import com.winsun.fruitmix.newdesign201804.file.offlineFile.OfflineFileActivity
import com.winsun.fruitmix.newdesign201804.file.sharedFolder.SharedFolderActivity
import com.winsun.fruitmix.newdesign201804.file.transmission.InjectTransmissionDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.TransmissionTaskActivity
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.InjectTransmissionTaskRepository
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.TASK_UUID_KEY
import com.winsun.fruitmix.newdesign201804.file.upload.FileBrowserActivity
import com.winsun.fruitmix.newdesign201804.login.LoginEntranceActivity
import com.winsun.fruitmix.newdesign201804.logout.InjectLogoutUseCase
import com.winsun.fruitmix.newdesign201804.mainpage.DrawerItem
import com.winsun.fruitmix.newdesign201804.mainpage.MainPage
import com.winsun.fruitmix.newdesign201804.user.preference.InjectUserPreference
import com.winsun.fruitmix.newdesign201804.user.preference.UserPreferenceContainer
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl
import com.winsun.fruitmix.util.SnackbarUtil
import com.winsun.fruitmix.util.Util
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel

const val FILE_BROWSER_REQUEST_CODE = 0x1001

private const val TAG = "FilePage"

class FilePage(val activity: BaseActivity) : MainPage, FileView {

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

        filePresenter = FilePresenter(InjectFileDataSource.inject(activity),
                noContentViewModel, loadingViewModel, filePageBinding, activity,
                this, activity.getCurrentUserUUID(), ThreadManagerImpl.getInstance(),
                InjectTransmissionTaskRepository.provideInstance(activity),
                InjectTransmissionDataSource.inject(activity),
                InjectStationFileRepository.provideStationFileRepository(activity),UserPreferenceContainer.userPreference,
                InjectUserPreference.inject(activity))

        val swipeRefreshLayout = filePageBinding.swipeRefreshLayout

        swipeRefreshLayout.setOnRefreshListener {
            filePresenter.refreshCurrentFolder()

            if (swipeRefreshLayout.isRefreshing)
                swipeRefreshLayout.isRefreshing = false

        }

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

        val logout =DrawerItem(R.drawable.ic_logout_black_24dp,activity.getString(R.string.logout),{

            InjectLogoutUseCase.inject(activity).logout()

            LoginEntranceActivity.start(activity)

        })

        drawerItems = mutableListOf(
                task, shareToSharedFolder, localFolder, tag,logout
        )

    }

    fun generateView() {

        R.layout.file_page
        R.layout.search_file_card

        R.layout.folder_item
        R.layout.file_item

        R.layout.folder_file_title

    }

    override fun getView(): View {
        return filePageBinding.root
    }

    override fun getActivity(): Activity {
        return activity
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
        filePresenter.onDestroy()
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {

    }

    override fun enterFileBrowserActivity() {

        val intent = Intent(activity, FileBrowserActivity::class.java)

        activity.startActivityForResult(intent, FILE_BROWSER_REQUEST_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == FILE_BROWSER_REQUEST_CODE && resultCode == Activity.RESULT_OK)
            filePresenter.onActivityResult(requestCode, resultCode, data)
        else if (requestCode == FILE_MOVE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            SnackbarUtil.showSnackBar(filePageBinding.root, Snackbar.LENGTH_SHORT, messageStr = "创建移动任务成功，请到任务列表中查看")

            filePresenter.handleMoveCopyTaskCreateSucceed(data!!.getStringExtra(TASK_UUID_KEY))

        } else if (requestCode == FILE_COPY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            SnackbarUtil.showSnackBar(filePageBinding.root, Snackbar.LENGTH_SHORT, messageStr = "创建拷贝任务成功，请到任务列表中查看")

            filePresenter.handleMoveCopyTaskCreateSucceed(data!!.getStringExtra(TASK_UUID_KEY))

        } else if (requestCode == FILE_SHARE_TO_SHARED_FOLDER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            SnackbarUtil.showSnackBar(filePageBinding.root, Snackbar.LENGTH_SHORT, messageStr = "创建分享任务成功，请到任务列表中查看")

            filePresenter.handleMoveCopyTaskCreateSucceed(data!!.getStringExtra(TASK_UUID_KEY))

        }

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

    fun moveBtnOnClick() {

        filePresenter.enterMovePageWhenSelectMode()

    }

    fun downloadBtnOnClick() {
        filePresenter.handleDownloadBtnOnClickWhenSelectFiles()
    }


}