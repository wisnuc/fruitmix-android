package com.winsun.fruitmix.newdesign201804.file.list.presenter

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.winsun.fruitmix.BR
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.*
import com.winsun.fruitmix.command.BaseAbstractCommand
import com.winsun.fruitmix.databinding.*
import com.winsun.fruitmix.dialog.BottomMenuGridDialogFactory
import com.winsun.fruitmix.dialog.BottomMenuListDialogFactory
import com.winsun.fruitmix.file.data.download.param.FileFromStationFolderDownloadParam
import com.winsun.fruitmix.file.data.model.*
import com.winsun.fruitmix.file.data.station.StationFileRepository
import com.winsun.fruitmix.http.HttpResponse
import com.winsun.fruitmix.interfaces.BaseView
import com.winsun.fruitmix.model.BottomMenuItem
import com.winsun.fruitmix.model.DivideBottomMenuItem
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.component.createFileDownloadParam
import com.winsun.fruitmix.newdesign201804.file.detail.FILE_UUID_KEY
import com.winsun.fruitmix.newdesign201804.file.detail.FileDetailActivity
import com.winsun.fruitmix.newdesign201804.file.list.FilePageActionListener
import com.winsun.fruitmix.newdesign201804.file.list.FilePageSelectActionListener
import com.winsun.fruitmix.newdesign201804.file.list.FileView
import com.winsun.fruitmix.newdesign201804.file.list.MainPageDividerItemDecoration
import com.winsun.fruitmix.newdesign201804.file.list.data.*
import com.winsun.fruitmix.newdesign201804.file.operation.FileOperation
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FileItemViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderFileTitleViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderItemViewModel
import com.winsun.fruitmix.newdesign201804.file.move.*
import com.winsun.fruitmix.newdesign201804.file.operation.CreateFolderUseCase
import com.winsun.fruitmix.newdesign201804.file.transmission.TransmissionDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.TransmissionTaskDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.*
import com.winsun.fruitmix.newdesign201804.user.preference.*
import com.winsun.fruitmix.parser.RemoteMkDirParser
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.BindingViewHolder
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.user.datasource.InjectUser
import com.winsun.fruitmix.util.FileUtil
import com.winsun.fruitmix.util.SnackbarUtil
import com.winsun.fruitmix.util.Util
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel
import kotlinx.android.synthetic.main.folder_item.view.*
import java.io.File
import java.util.concurrent.Callable
import kotlin.Comparator

private const val SPAN_COUNT = 2

private val mSelectFiles = mutableListOf<AbstractFile>()
private var mIsSelectMode = false

class FilePresenter(val fileDataSource: FileDataSource, val noContentViewModel: NoContentViewModel,
                    val loadingViewModel: LoadingViewModel, val filePageBinding: FilePageBinding,
                    val baseView: BaseView, val fileView: FileView,
                    val currentUserUUID: String, val threadManager: ThreadManager,
                    val transmissionTaskDataSource: TransmissionTaskDataSource,
                    val transmissionDataSource: TransmissionDataSource,
                    val stationFileRepository: StationFileRepository,
                    val userPreference: UserPreference,
                    val userPreferenceDataSource: UserPreferenceDataSource) : TaskStateObserver {

    private val contentLayout = filePageBinding.contentLayout

    private lateinit var fileRecyclerViewAdapter: FileRecyclerViewAdapter

    private val currentFolderItems: MutableList<AbstractRemoteFile> = mutableListOf()

    private val context: Context = filePageBinding.fileRecyclerView.context

    private val gridLayoutManager = GridLayoutManager(context, SPAN_COUNT)

    private val linearLayoutManager = LinearLayoutManager(context)

    private val viewItems = mutableListOf<ViewItem>()

    private lateinit var mainPageDividerItemDecoration: MainPageDividerItemDecoration

    private val filePageSelectActionListeners = mutableListOf<FilePageSelectActionListener>()
    private val filePageActionListeners = mutableListOf<FilePageActionListener>()

    private var rootFolderUUID = ""

    private var currentFolderUUID = rootFolderUUID
    private var currentFolderName = ""

    private val retrievedFolderUUIDs = mutableListOf<String>()
    private val retrievedFolderNames = mutableListOf<String>()

    private val handleTasks = mutableListOf<Task>()

    private lateinit var sortComparator: Comparator<ItemContent>

    private val sortPolicy = userPreference.fileSortPolicy
    private val fileViewModePolicy = userPreference.fileViewModePolicy

    fun initView() {

        val currentUserUUID = InjectSystemSettingDataSource.provideSystemSettingDataSource(context)
                .currentLoginUserUUID

        val currentUser = InjectUser.provideRepository(context).getUserByUUID(currentUserUUID)

        rootFolderUUID = currentUser.home

        val fileOperation = FileOperation(currentUserUUID, threadManager, transmissionTaskDataSource,
                contentLayout, fileDataSource, baseView, fileView,
                { position ->

                    fileRecyclerViewAdapter.notifyItemChanged(position)
                },
                { position ->

                    viewItems.removeAt(position)

                    val itemSize = viewItems.filter {
                        it is ItemFolder || it is ItemFile
                    }.size

                    if (itemSize == 0)
                        noContentViewModel.showNoContent.set(true)
                    else {

                        fileRecyclerViewAdapter.setItemList(viewItems)

                        fileRecyclerViewAdapter.notifyItemRemoved(position)

                    }

                })

        fileRecyclerViewAdapter = FileRecyclerViewAdapter({ remoteFile, position ->
            doHandleItemOnClick(remoteFile, position)
        }, {
            doHandleOnLongClick(it)
        }, { remoteFile, position ->

            fileOperation.doShowFileMenuBottomDialog(context, remoteFile, position)

//            doShowFileMenuBottomDialog(context, remoteFile, position)
        }, { showSortBottomDialog(context) },
                userPreference.fileSortPolicy)

        gridLayoutManager.spanSizeLookup.isSpanIndexCacheEnabled = true

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return fileRecyclerViewAdapter.getSpanSize(position)
            }
        }

        initRecyclerView()

        initSortComparator()

        gotoNextFolder(rootFolderUUID, context.getString(R.string.my_private_drive))

        updateToggleOrientation()

    }

    private fun updateToggleOrientation() {
        filePageActionListeners.forEach {

            if (fileViewModePolicy.getCurrentFileViewMode() == FileViewMode.LIST) {
                it.updateToggleOrientationIcon(R.drawable.file_list_mode)
            } else
                it.updateToggleOrientationIcon(R.drawable.view_mode)

        }
    }

    fun registerFilePageSelectActionListener(filePageSelectActionListener: FilePageSelectActionListener) {
        filePageSelectActionListeners.add(filePageSelectActionListener)
    }

    fun unregisterFilePageSelectActionListener(filePageSelectActionListener: FilePageSelectActionListener) {
        filePageSelectActionListeners.remove(filePageSelectActionListener)
    }

    fun registerFilePageActionListener(filePageActionListener: FilePageActionListener) {
        filePageActionListeners.add(filePageActionListener)
    }

    fun unregisterFilePageActionListener(filePageActionListener: FilePageActionListener) {
        filePageActionListeners.remove(filePageActionListener)
    }

    private fun initRecyclerView() {

        fileRecyclerViewAdapter.fileViewMode =
                userPreference.fileViewModePolicy.getCurrentFileViewMode()

        filePageBinding.fileRecyclerView.adapter = fileRecyclerViewAdapter

        filePageBinding.addFab.setOnClickListener {

            showAddDialog()

        }

        filePageBinding.fileRecyclerView.itemAnimator = DefaultItemAnimator()

        mainPageDividerItemDecoration = MainPageDividerItemDecoration(SPAN_COUNT, 0)

        setRecyclerViewLayoutManager()

    }

    private fun initSortComparator() {
        sortComparator = Comparator { o1, o2 ->
            when (sortPolicy.getCurrentSortMode()) {
                SortMode.NAME -> {
                    return@Comparator o1.getFileName().compareTo(o2.getFileName())
                }
                SortMode.SIZE -> {

                    when {
                        o1.getFileSize() > o2.getFileSize() -> return@Comparator 1
                        o1.getFileSize() < o2.getFileSize() -> return@Comparator -1
                        else -> return@Comparator 0
                    }

                }
                SortMode.CREATE_TIME -> {

                    when {
                        o1.getFileModifyTime() > o2.getFileModifyTime() -> return@Comparator 1
                        o1.getFileModifyTime() < o2.getFileModifyTime() -> return@Comparator -1
                        else -> return@Comparator 0
                    }

                }
                SortMode.MODIFY_TIME -> {

                    when {
                        o1.getFileModifyTime() > o2.getFileModifyTime() -> return@Comparator 1
                        o1.getFileModifyTime() < o2.getFileModifyTime() -> return@Comparator -1
                        else -> return@Comparator 0
                    }

                }
            }

        }
    }

    fun refreshCurrentFolder() {
        loadingViewModel.showLoading.set(true)

        fileDataSource.getFile(rootFolderUUID, currentFolderUUID, currentFolderName,object : BaseLoadDataCallback<AbstractRemoteFile> {

            override fun onFail(operationResult: OperationResult?) {
                loadingViewModel.showLoading.set(false)
                noContentViewModel.showNoContent.set(true)
            }

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                handleGetFileSucceed(data)

            }

        })
    }

    private fun refreshData() {
        val folderViewItems = mutableListOf<ItemContent>()
        val fileViewItems = mutableListOf<ItemContent>()

        currentFolderItems.forEach {

            if (it.isFolder) {
                folderViewItems.add(ItemFolder(FolderItemViewModel(it as RemoteFolder)))
            } else {
                fileViewItems.add(ItemFile(FileItemViewModel(it as RemoteFile)))
            }

        }

        folderViewItems.sortWith(sortComparator)
        fileViewItems.sortWith(sortComparator)

        if (sortPolicy.getCurrentSortDirection() == SortDirection.NEGATIVE) {
            folderViewItems.reverse()
            fileViewItems.reverse()
        }

        viewItems.clear()

        val fileTitleViewModel = FolderFileTitleViewModel()

        if (folderViewItems.size > 0) {

            val folderFileTitleViewModel = FolderFileTitleViewModel()
            folderFileTitleViewModel.showSortBtn.set(true)

            viewItems.add(ItemFolderHead(folderFileTitleViewModel))
            viewItems.addAll(folderViewItems)

            fileTitleViewModel.showSortBtn.set(false)
            viewItems.add(ItemFileHead(fileTitleViewModel))
            viewItems.addAll(fileViewItems)

        } else {

            fileTitleViewModel.showSortBtn.set(true)
            viewItems.add(ItemFileHead(fileTitleViewModel))
            viewItems.addAll(fileViewItems)

        }

        mainPageDividerItemDecoration.totalItemCount = viewItems.size

        fileRecyclerViewAdapter.setItemList(viewItems)
        fileRecyclerViewAdapter.notifyDataSetChanged()

    }

    private fun showSortBottomDialog(context: Context) {

        val bottomMenuItems = mutableListOf<BottomMenuItem>()

        val bottomMenuItem = BottomMenuItem(
                getSortModeIconResID(SortMode.NAME),
                context.getString(R.string.sort_by_name), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                sortPolicy.setCurrentSortMode(SortMode.NAME)
                userPreferenceDataSource.updateUserPreference(currentUserUUID, userPreference)

                refreshData()

            }

        })

        bottomMenuItem.rightResID = getBottomMenuRightResID(SortMode.NAME)

        bottomMenuItems.add(bottomMenuItem)

        val sortByModifyTimeItem = BottomMenuItem(getSortModeIconResID(SortMode.MODIFY_TIME), context.getString(R.string.sort_by_modify_time), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                sortPolicy.setCurrentSortMode(SortMode.MODIFY_TIME)
                userPreferenceDataSource.updateUserPreference(currentUserUUID, userPreference)

                refreshData()

            }

        })

        sortByModifyTimeItem.rightResID = getBottomMenuRightResID(SortMode.MODIFY_TIME)
        bottomMenuItems.add(sortByModifyTimeItem)

        val sortByCreateTimeItem = BottomMenuItem(getSortModeIconResID(SortMode.CREATE_TIME), context.getString(R.string.sort_by_create_time), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                sortPolicy.setCurrentSortMode(SortMode.CREATE_TIME)
                userPreferenceDataSource.updateUserPreference(currentUserUUID, userPreference)

                refreshData()

            }

        })

        sortByCreateTimeItem.rightResID = getBottomMenuRightResID(SortMode.CREATE_TIME)
        bottomMenuItems.add(sortByCreateTimeItem)

        val sortByCapacityItem = BottomMenuItem(getSortModeIconResID(SortMode.SIZE), context.getString(R.string.sort_by_capacity), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                sortPolicy.setCurrentSortMode(SortMode.SIZE)
                userPreferenceDataSource.updateUserPreference(currentUserUUID, userPreference)

                refreshData()

            }

        })

        sortByCapacityItem.rightResID = getBottomMenuRightResID(SortMode.SIZE)
        bottomMenuItems.add(sortByCapacityItem)

        BottomMenuListDialogFactory(bottomMenuItems).createDialog(context).show()

    }

    private fun getSortModeIconResID(sortMode: SortMode): Int {
        return if (sortPolicy.getCurrentSortMode() == sortMode) {

            if (sortPolicy.getCurrentSortDirection() == SortDirection.POSITIVE)
                R.drawable.black_up_arrow
            else
                R.drawable.black_down_arrow

        } else 0
    }

    private fun getBottomMenuRightResID(sortMode: SortMode): Int {
        return if (sortPolicy.getCurrentSortMode() == sortMode)
            R.drawable.green_done
        else
            0
    }

    private fun showAddDialog() {
        val bottomMenuItems = mutableListOf<BottomMenuItem>()

        val bottomMenuItem = BottomMenuItem(R.drawable.bottom_menu_folder, context.getString(R.string.folder), object : BaseAbstractCommand() {
            override fun execute() {
                super.execute()

                createFolder()
            }
        })

        bottomMenuItems.add(bottomMenuItem)

        val uploadBottomMenuItem = BottomMenuItem(R.drawable.upload, context.getString(R.string.upload), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                fileView.enterFileBrowserActivity()

            }

        })

        bottomMenuItems.add(uploadBottomMenuItem)

        val magnetBottomMenuItem = BottomMenuItem(R.drawable.magnet_link, context.getString(R.string.magnet_link), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                createMagnetTransmission()
            }

        })

        bottomMenuItems.add(magnetBottomMenuItem)

        BottomMenuGridDialogFactory(bottomMenuItems).createDialog(context).show()
    }

    private fun createFolder() {

        val createFolderUseCase = CreateFolderUseCase(fileDataSource, baseView, filePageBinding.root,
                { newFolder ->

                    currentFolderItems.add(newFolder)

                    refreshView()

                })

        val parentFolder = RemoteFolder()
        parentFolder.rootFolderUUID = rootFolderUUID
        parentFolder.uuid = currentFolderUUID

        createFolderUseCase.createFolder(context, parentFolder)

/*        val editText = EditText(context)

        editText.hint = context.getString(R.string.no_title_folder)

        AlertDialog.Builder(context).setTitle(context.getString(R.string.new_create) + context.getString(R.string.folder))
                .setView(editText)
                .setPositiveButton(R.string.new_create) { dialog, which ->

                    var folderName = editText.text.toString()

                    if (folderName.isEmpty())
                        folderName = editText.hint.toString()

                    doCreateFolder(folderName)

                }
                .setNegativeButton(R.string.cancel) { dialog, which -> }
                .create().show()*/

    }

    private fun doCreateFolder(folderName: String) {

        baseView.showProgressDialog(context.getString(R.string.operating_title, context.getString(R.string.create)))

        fileDataSource.createFolder(folderName, rootFolderUUID, currentFolderUUID, object : BaseOperateDataCallback<HttpResponse> {

            override fun onSucceed(data: HttpResponse?, result: OperationResult?) {

                baseView.dismissDialog()

                val parser = RemoteMkDirParser()

                val newFolder = parser.parse(data?.responseData)

                currentFolderItems.add(newFolder)

                refreshData()

            }

            override fun onFail(operationResult: OperationResult?) {

                baseView.dismissDialog()

                SnackbarUtil.showSnackBar(filePageBinding.root, Snackbar.LENGTH_SHORT,
                        messageStr = operationResult?.getResultMessage(context)!!)

            }

        })


    }

    private fun createMagnetTransmission() {

        val editText = EditText(context)

        editText.hint = "magnet:?xt=urn:btih:"

        AlertDialog.Builder(context).setTitle(context.getString(R.string.magnet_link))
                .setView(editText)
                .setPositiveButton(R.string.new_create) { dialog, which ->

                    val magnetUrl = editText.text.toString()

                    Log.d("magnetTest", magnetUrl.length.toString())
                    Log.d("magnetTest", (!magnetUrl.startsWith("magnet:?xt=urn:btih:")).toString())

                    if (magnetUrl.length < 60 || !magnetUrl.startsWith("magnet:?xt=urn:btih:")) {

                        SnackbarUtil.showSnackBar(contentLayout, Snackbar.LENGTH_SHORT, R.string.magnet_illegal)

                        return@setPositiveButton
                    }

                    doCreateMagnetTransmission(magnetUrl)

                }
                .setNegativeButton(R.string.cancel) { dialog, which -> }
                .create().show()

    }

    private fun doCreateMagnetTransmission(magnetUrl: String) {

        baseView.showProgressDialog(baseView.getString(R.string.operating_title, baseView.getString(R.string.create)))

        transmissionDataSource.postMagnetTransmission(currentFolderUUID, magnetUrl, object : BaseOperateCallback {
            override fun onFail(operationResult: OperationResult?) {

                baseView.dismissDialog()

                SnackbarUtil.showSnackBar(contentLayout, Snackbar.LENGTH_SHORT, messageStr = operationResult!!.getResultMessage(context))
            }

            override fun onSucceed() {

                baseView.dismissDialog()

                SnackbarUtil.showSnackBar(contentLayout, Snackbar.LENGTH_SHORT, messageStr =
                baseView.getString(R.string.success, baseView.getString(R.string.create)))
            }
        })

    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val abstractFiles = SelectMoveFileDataSource.getSelectFiles()

        abstractFiles.forEach {

            if(it is LocalFile){
                val filePath = it.path

                val future = threadManager.runOnCacheThread(Callable<String> {
                    Util.calcSHA256OfFile(filePath)
                })

                val fileHash = future.get()

                it.fileHash = fileHash
                it.size = File(filePath).length()

                val fileUploadParam = FileUploadParam(rootFolderUUID, currentFolderUUID, it)

                val uploadTask = UploadTask(Util.createLocalUUid(), currentUserUUID, it, fileDataSource, fileUploadParam, threadManager)

                transmissionTaskDataSource.addTransmissionTask(uploadTask)

                uploadTask.registerObserver(this)
                handleTasks.add(uploadTask)
            }else if(it is LocalFolder){

                val fileUploadParam = FileUploadParam(rootFolderUUID, currentFolderUUID, it)

                val uploadFolderTask = UploadFolderTask(stationFileRepository,
                        Util.createLocalUUid(),currentUserUUID,it,fileDataSource,fileUploadParam,threadManager)

                transmissionTaskDataSource.addTransmissionTask(uploadFolderTask)

                uploadFolderTask.registerObserver(this)
                handleTasks.add(uploadFolderTask)

            }

        }

        SnackbarUtil.showSnackBar(contentLayout, Snackbar.LENGTH_SHORT, R.string.add_task_hint)

    }

    fun handleMoveCopyTaskCreateSucceed(taskUUID: String) {

        val task = transmissionTaskDataSource.getTransmissionTaskInCache(taskUUID)

        if (task != null && (task is TransmissionTask)) {

            if (task.getCurrentState().getType() == StateType.FINISH)
                refreshCurrentFolder()
            else {

                task.registerObserver(this)

                handleTasks.add(task)
            }

        }

    }

    override fun notifyStateChanged(currentState: TaskState) {
        if (currentState.getType() == StateType.FINISH)
            refreshCurrentFolder()

    }

    fun onDestroy() {

        handleTasks.forEach {

            if (it is TransmissionTask)
                it.stopRefresh()

            it.unregisterObserver(this)
        }

        handleTasks.clear()

    }

    fun handleMoreIvClick() {

        val bottomMenuItems = listOf<BottomMenuItem>(

                BottomMenuItem(0, context.getString(R.string.choose_text), object : BaseAbstractCommand() {

                    override fun execute() {
                        super.execute()

                        enterSelectMode()
                    }

                }),
                BottomMenuItem(0, context.getString(R.string.select_all), object : BaseAbstractCommand() {

                    override fun execute() {
                        super.execute()

                        mSelectFiles.addAll(currentFolderItems)

                        enterSelectMode()

                    }

                })

        )

        BottomMenuListDialogFactory(bottomMenuItems).createDialog(context).show()

    }

    fun toggleOrientation() {

        fileViewModePolicy.setCurrentFileViewMode(
                if (fileViewModePolicy.getCurrentFileViewMode() == FileViewMode.GRID) FileViewMode.LIST
                else FileViewMode.GRID)

        updateToggleOrientation()

        userPreferenceDataSource.updateUserPreference(currentUserUUID, userPreference)

        setRecyclerViewLayoutManager()

        fileRecyclerViewAdapter.fileViewMode = fileViewModePolicy.getCurrentFileViewMode()

        fileRecyclerViewAdapter.setItemList(viewItems)
        fileRecyclerViewAdapter.notifyDataSetChanged()

    }

    private fun setRecyclerViewLayoutManager() {

        val fileViewMode = fileViewModePolicy.getCurrentFileViewMode()

        if (fileViewMode == FileViewMode.GRID) {

            filePageBinding.fileRecyclerView.layoutManager = gridLayoutManager

            filePageBinding.fileRecyclerView.addItemDecoration(mainPageDividerItemDecoration)

        } else if (fileViewMode == FileViewMode.LIST) {

            filePageBinding.fileRecyclerView.layoutManager = linearLayoutManager

            filePageBinding.fileRecyclerView.removeItemDecoration(mainPageDividerItemDecoration)

        }

    }

    private fun doHandleOnLongClick(abstractFile: AbstractFile) {

        if (mIsSelectMode)
            return

        mSelectFiles.add(abstractFile)

        enterSelectMode()

    }


    private fun doHandleItemOnClick(abstractFile: AbstractFile, position: Int) {

        if (mIsSelectMode) {

            if (mSelectFiles.contains(abstractFile))
                mSelectFiles.remove(abstractFile)
            else
                mSelectFiles.add(abstractFile)

            if (mSelectFiles.isEmpty()) {
                quitSelectMode()
            } else {

                fileRecyclerViewAdapter.selectFiles = mSelectFiles
                fileRecyclerViewAdapter.isSelectMode = mIsSelectMode

                fileRecyclerViewAdapter.notifyItemChanged(position)

                filePageSelectActionListeners.forEach {
                    it.notifySelectCountChange(mSelectFiles.size)
                }

            }

        } else {

            if (abstractFile is AbstractRemoteFile) {

                if (abstractFile is RemoteFolder) {

                    gotoNextFolder(abstractFile.uuid, abstractFile.name)

                } else {

                    openFileAfterOnClick(abstractFile)

                }


            } else {


            }

        }


    }

    private fun openFileAfterOnClick(abstractFile: AbstractRemoteFile) {
        if (FileUtil.checkFileExistInDownloadFolder(abstractFile.name))
            FileUtil.openAbstractRemoteFile(context, abstractFile.name)
        else {

            val fileDownloadParam = FileFromStationFolderDownloadParam(abstractFile.uuid,
                    abstractFile.parentFolderUUID, abstractFile.rootFolderUUID, abstractFile.name)

            val task = DownloadTask(Util.createLocalUUid(), currentUserUUID, abstractFile, fileDataSource, fileDownloadParam,
                    currentUserUUID, threadManager)

            task.init()

            val taskProgressDialog = ProgressDialog(context)
            taskProgressDialog.setTitle(context.getString(R.string.downloading))
            taskProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            taskProgressDialog.isIndeterminate = false

            taskProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), { dialog, which ->

                task.cancelTask()

                dialog.dismiss()

            })

            taskProgressDialog.setCancelable(false)
            taskProgressDialog.max = 100

            task.registerObserver(object : TaskStateObserver {
                override fun notifyStateChanged(currentState: TaskState) {

                    if (currentState is StartingTaskState) {

                        taskProgressDialog.progress = currentState.progress

                        taskProgressDialog.setProgressNumberFormat(currentState.speed)

                    } else if (currentState is FinishTaskState) {

                        taskProgressDialog.dismiss()

                        FileUtil.openAbstractRemoteFile(context, abstractFile.name)

                        task.unregisterObserver(this)

                    }

                }
            })

            task.startTask()

            taskProgressDialog.show()

        }
    }

    private fun handleGetFileSucceed(data: MutableList<AbstractRemoteFile>?) {

        loadingViewModel.showLoading.set(false)

        currentFolderItems.clear()
        currentFolderItems.addAll(data!!)

        refreshView()

    }

    private fun refreshView() {
        if (currentFolderItems.size == 0)
            noContentViewModel.showNoContent.set(true)
        else {

            noContentViewModel.showNoContent.set(false)

            refreshData()

        }
    }


    private fun notifyFolderLevelChanged() {
        if (notRootFolder()) {

            filePageActionListeners.forEach {
                it.notifyFolderLevelChanged(false, currentFolderName, currentFolderUUID)
            }

        } else {

            filePageActionListeners.forEach {
                it.notifyFolderLevelChanged(true)
            }

        }
    }


    fun useDefaultBackPressFunction(): Boolean {

        return when {
            mIsSelectMode -> false
            notRootFolder() -> false
            else -> true
        }
    }

    private fun notRootFolder(): Boolean {
        return currentFolderUUID != rootFolderUUID
    }

    fun onBackPressed() {

        if (mIsSelectMode)
            quitSelectMode()
        else if (notRootFolder()) {
            goToPreFolder()
        }

    }

    private fun gotoNextFolder(uuid: String, name: String) {

        loadingViewModel.showLoading.set(true)

        fileDataSource.getFile(rootFolderUUID, uuid, name,object : BaseLoadDataCallback<AbstractRemoteFile> {
            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                currentFolderUUID = uuid
                currentFolderName = name

                retrievedFolderNames.add(currentFolderName)
                retrievedFolderUUIDs.add(currentFolderUUID)

                handleGetFileSucceed(data)

                notifyFolderLevelChanged()

            }

            override fun onFail(operationResult: OperationResult?) {

                currentFolderUUID = uuid
                currentFolderName = name

                retrievedFolderNames.add(currentFolderName)
                retrievedFolderUUIDs.add(currentFolderUUID)

                loadingViewModel.showLoading.set(false)
                noContentViewModel.showNoContent.set(true)

            }
        })

    }

    private fun goToPreFolder() {

        retrievedFolderUUIDs.removeAt(retrievedFolderUUIDs.lastIndex)
        val uuid = retrievedFolderUUIDs.last()

        retrievedFolderNames.removeAt(retrievedFolderNames.lastIndex)
        val name = retrievedFolderNames.last()

        loadingViewModel.showLoading.set(true)

        fileDataSource.getFile(rootFolderUUID, uuid,name, object : BaseLoadDataCallback<AbstractRemoteFile> {
            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                currentFolderUUID = uuid
                currentFolderName = name

                handleGetFileSucceed(data)

                notifyFolderLevelChanged()

            }

            override fun onFail(operationResult: OperationResult?) {

                currentFolderUUID = uuid
                currentFolderName = name

                loadingViewModel.showLoading.set(false)
                noContentViewModel.showNoContent.set(true)

            }
        })

    }

    private fun enterSelectMode() {

        filePageBinding.addFab.visibility = View.INVISIBLE

        mIsSelectMode = true

        fileRecyclerViewAdapter.isSelectMode = mIsSelectMode
        fileRecyclerViewAdapter.selectFiles = mSelectFiles

        fileRecyclerViewAdapter.notifyDataSetChanged()

        filePageSelectActionListeners.forEach {
            it.notifySelectModeChange(mIsSelectMode)

            it.notifySelectCountChange(mSelectFiles.size)
        }

    }

    fun quitSelectMode() {

        filePageBinding.addFab.visibility = View.VISIBLE

        mSelectFiles.clear()

        mIsSelectMode = false

        fileRecyclerViewAdapter.isSelectMode = mIsSelectMode
        fileRecyclerViewAdapter.selectFiles = mSelectFiles

        fileRecyclerViewAdapter.notifyDataSetChanged()

        filePageSelectActionListeners.forEach {
            it.notifySelectModeChange(mIsSelectMode)

            it.notifySelectCountChange(mSelectFiles.size)
        }

    }

    private fun doShowFileMenuBottomDialog(context: Context, abstractFile: AbstractFile, position: Int) {

        val bottomMenuItems = mutableListOf<BottomMenuItem>()

        bottomMenuItems.add(BottomMenuItem(R.drawable.modify_icon, context.getString(R.string.rename), object : BaseAbstractCommand() {
            override fun execute() {
                super.execute()

                rename(abstractFile, position)
            }
        }))

        bottomMenuItems.add(BottomMenuItem(R.drawable.black_move, context.getString(R.string.move_to), object : BaseAbstractCommand() {
            override fun execute() {
                super.execute()
                enterMovePage(abstractFile)
            }
        }))

        if (!abstractFile.isFolder) {

            val bottomMenuItem = FileWithSwitchBottomItem(R.drawable.offline_available, context.getString(R.string.offline_available),
                    object : BaseAbstractCommand() {}, {

                val abstractRemoteFile = abstractFile as AbstractRemoteFile

                val fileDownloadParam = abstractRemoteFile.createFileDownloadParam()

                val downloadTask = DownloadTask(Util.createLocalUUid(), currentUserUUID, abstractRemoteFile, fileDataSource, fileDownloadParam,
                        currentUserUUID, threadManager)

                transmissionTaskDataSource.addTransmissionTask(downloadTask)

                SnackbarUtil.showSnackBar(contentLayout, Snackbar.LENGTH_SHORT, R.string.add_task_hint)

                it.dismissDialog()

            })

            bottomMenuItems.add(bottomMenuItem)

            bottomMenuItems.add(BottomMenuItem(R.drawable.open_with_other_app, context.getString(R.string.open_with_other_app), object : BaseAbstractCommand() {

                override fun execute() {
                    super.execute()

                    openFileAfterOnClick(abstractFile as AbstractRemoteFile)

                }

            }))

        }

        bottomMenuItems.add(DivideBottomMenuItem())

        if (!abstractFile.isFolder) {

            bottomMenuItems.add(BottomMenuItem(R.drawable.make_a_copy, context.getString(R.string.make_a_copy), object : BaseAbstractCommand() {

                override fun execute() {
                    super.execute()

                }

            }))

            bottomMenuItems.add(BottomMenuItem(R.drawable.edit_tag, context.getString(R.string.edit_tag), object : BaseAbstractCommand() {}))

        }

        bottomMenuItems.add(BottomMenuItem(R.drawable.share_to_shared_folder, context.getString(R.string.share_to_shared_folder), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                enterShareToSharedFolderPage(abstractFile)
            }

        }))

        bottomMenuItems.add(BottomMenuItem(R.drawable.copy_to, context.getString(R.string.copy_to), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                enterCopyPage(abstractFile)
            }

        }))

        bottomMenuItems.add(BottomMenuItem(R.drawable.delete_download_task, context.getString(R.string.delete_text), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                deleteFile(abstractFile, position)
            }

        }))

        val dialog = FileMenuBottomDialogFactory(abstractFile, bottomMenuItems, {

            FileDetailActivity.start(abstractFile as AbstractRemoteFile,context)

        }).createDialog(context)

        dialog.show()

    }

    private fun rename(abstractFile: AbstractFile, position: Int) {

        val editText = EditText(context)
        editText.hint = abstractFile.name

        AlertDialog.Builder(context).setTitle(context.getString(R.string.rename))
                .setView(editText)
                .setPositiveButton(R.string.rename, { dialog, which ->

                    val oldName = abstractFile.name
                    val newName = editText.text.toString()

                    if (newName.isEmpty()) {
                        SnackbarUtil.showSnackBar(contentLayout, Snackbar.LENGTH_SHORT, R.string.new_name_empty_hint)
                    } else if (oldName != newName) {
                        doRename(oldName, newName, abstractFile, position)
                    }

                })
                .setNegativeButton(R.string.cancel, { dialog, which -> })
                .create().show()

    }

    private fun doRename(oldName: String, newName: String, abstractFile: AbstractFile, position: Int) {

        baseView.showProgressDialog(context.getString(R.string.operating_title, context.getString(R.string.rename)))

        fileDataSource.renameFile(oldName, newName, rootFolderUUID, currentFolderUUID, object : BaseOperateCallback {

            override fun onFail(operationResult: OperationResult?) {

                baseView.dismissDialog()
                SnackbarUtil.showSnackBar(contentLayout, Snackbar.LENGTH_SHORT,
                        messageStr = operationResult!!.getResultMessage(context))
            }

            override fun onSucceed() {

                baseView.dismissDialog()

                abstractFile.name = newName

                fileRecyclerViewAdapter.notifyItemChanged(position)

            }

        })

    }


    private fun enterMovePage(abstractFile: AbstractFile) {

        mSelectFiles.add(abstractFile)

        MoveFileActivity.start(fileView.getActivity(), mSelectFiles, FILE_OPERATE_MOVE)

    }

    private fun enterCopyPage(abstractFile: AbstractFile) {

        mSelectFiles.add(abstractFile)

        MoveFileActivity.start(fileView.getActivity(), mSelectFiles, FILE_OPERATE_COPY)

    }

    private fun enterShareToSharedFolderPage(abstractFile: AbstractFile) {

        mSelectFiles.add(abstractFile)

        MoveFileActivity.start(fileView.getActivity(), mSelectFiles, FILE_OPERATE_SHARE_TO_SHARED_FOLDER)

    }

    private fun deleteFile(abstractFile: AbstractFile, position: Int) {

        AlertDialog.Builder(context).setTitle(R.string.delete_or_not)
                .setPositiveButton(R.string.delete_text,
                        { dialog, which ->

                            doDeleteFile(abstractFile, position)
                        })
                .setNegativeButton(R.string.cancel, null)
                .create().show()

    }

    private fun doDeleteFile(abstractFile: AbstractFile, position: Int) {

        baseView.showProgressDialog(context.getString(R.string.operating_title, context.getString(R.string.delete_text)))

        fileDataSource.deleteFile(abstractFile.name, rootFolderUUID, currentFolderUUID, object : BaseOperateCallback {

            override fun onFail(operationResult: OperationResult?) {

                baseView.dismissDialog()
                SnackbarUtil.showSnackBar(contentLayout, Snackbar.LENGTH_SHORT,
                        messageStr = operationResult!!.getResultMessage(context))
            }

            override fun onSucceed() {

                baseView.dismissDialog()

                viewItems.removeAt(position)

                fileRecyclerViewAdapter.setItemList(viewItems)

                fileRecyclerViewAdapter.notifyItemRemoved(position)

            }

        })

    }

    fun enterMovePageWhenSelectMode() {

        if (mSelectFiles.isEmpty())
            SnackbarUtil.showSnackBar(contentLayout, Snackbar.LENGTH_SHORT, R.string.no_select_file)
        else {
            MoveFileActivity.start(fileView.getActivity(), mSelectFiles, FILE_OPERATE_MOVE)
        }

    }

    fun handleDownloadBtnOnClickWhenSelectFiles() {

        if (mSelectFiles.isEmpty())
            SnackbarUtil.showSnackBar(contentLayout, Snackbar.LENGTH_SHORT, R.string.no_select_file)
        else {

            mSelectFiles.forEach {

                val abstractRemoteFile = it as AbstractRemoteFile

                val downloadTask =

                        if (it.isFolder)
                            DownloadFolderTask(stationFileRepository, Util.createLocalUUid(), currentUserUUID,
                                    abstractRemoteFile, fileDataSource, abstractRemoteFile.createFileDownloadParam(),
                                    currentUserUUID, threadManager)
                        else
                            DownloadTask(Util.createLocalUUid(), currentUserUUID,
                                    abstractRemoteFile, fileDataSource, abstractRemoteFile.createFileDownloadParam(),
                                    currentUserUUID, threadManager)

                transmissionTaskDataSource.addTransmissionTask(downloadTask)

            }

            mSelectFiles.clear()

            SnackbarUtil.showSnackBar(contentLayout, Snackbar.LENGTH_SHORT, R.string.add_task_hint)

        }

    }

}

class FileRecyclerViewAdapter(val handleItemOnClick: (abstractFile: AbstractFile, position: Int) -> Unit,
                              val handleItemOnLongClick: (abstractFile: AbstractFile) -> Unit,
                              val doHandleMoreBtnOnClick: (abstractFile: AbstractFile, position: Int) -> Unit,
                              val handleSortBtnOnClick: () -> Unit = {},
                              val sortPolicy: FileSortPolicy) : BaseRecyclerViewAdapter<BindingViewHolder, ViewItem>() {

    var fileViewMode = FileViewMode.GRID

    var selectFiles = mutableListOf<AbstractFile>()
    var isSelectMode = false

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BindingViewHolder {

        val viewDataBinding = when (viewType) {

            ITEM_FOLDER_HEAD, ITEM_FILE_HEAD -> FolderFileTitleBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
            GRID_ITEM_FOLDER -> {
                FolderItemBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
            }

            LIST_ITEM_FOLDER ->
                FileFolderListItemBinding.inflate(LayoutInflater.from(parent?.context), parent, false)

            GRID_ITEM_FILE ->
                FileItemBinding.inflate(LayoutInflater.from(parent?.context), parent, false)

            LIST_ITEM_FILE -> {
                FileFolderListItemBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
            }
            else -> throw IllegalArgumentException("file view type is illegal")
        }

        return BindingViewHolder(viewDataBinding)

    }

    override fun onBindViewHolder(holder: BindingViewHolder?, position: Int) {

        val viewItem = mItemList[position]

        when (getItemViewType(position)) {
            ITEM_FOLDER_HEAD, ITEM_FILE_HEAD -> {

                val itemFolderHead: ItemFolderHead = (viewItem as ItemFolderHead)

                val folderFileTitleBinding = holder?.viewDataBinding as FolderFileTitleBinding

                val context = folderFileTitleBinding.root.context

                itemFolderHead.folderFileTitleViewModel.sortModeText.set(when (sortPolicy.getCurrentSortMode()) {
                    SortMode.NAME -> R.string.name
                    SortMode.CREATE_TIME -> R.string.create_time
                    SortMode.SIZE -> R.string.capacity
                    SortMode.MODIFY_TIME -> R.string.modify_time
                })

                itemFolderHead.folderFileTitleViewModel.sortDirectionIconResID.set(when (sortPolicy.getCurrentSortDirection()) {
                    SortDirection.POSITIVE -> R.drawable.green_up_arrow
                    SortDirection.NEGATIVE -> R.drawable.green_down_arrow
                })

                folderFileTitleBinding.folderFileTitleViewModel = itemFolderHead.folderFileTitleViewModel

                folderFileTitleBinding.sortLayout.setOnClickListener {
                    handleSortBtnOnClick()
                }

            }
            GRID_ITEM_FILE, LIST_ITEM_FILE -> {

                val rootView = holder?.viewDataBinding?.root

                val itemFile = viewItem as ItemFile

                itemFile.fileItemViewModel.doHandleMoreBtnOnClick = {
                    doHandleMoreBtnOnClick(itemFile.getFile(), position)
                }

                itemFile.fileItemViewModel.isSelectMode.set(isSelectMode)
                itemFile.fileItemViewModel.isSelected.set(selectFiles.contains(itemFile.getFile()))

                itemFile.fileItemViewModel.init()

                holder?.viewDataBinding?.setVariable(BR.fileItemViewModel, itemFile.fileItemViewModel)

                rootView?.setOnClickListener {

                    handleItemOnClick(itemFile.getFile(), position)

                }

                rootView?.setOnLongClickListener {

                    handleItemOnLongClick(itemFile.getFile())

                    return@setOnLongClickListener true

                }

            }

            GRID_ITEM_FOLDER, LIST_ITEM_FOLDER -> {

                val itemFolder = viewItem as ItemFolder

                itemFolder.folderFileTitleViewModel.doHandleMoreBtnOnClick = {
                    doHandleMoreBtnOnClick(itemFolder.getFile(), position)
                }

                val isSelected = selectFiles.contains(itemFolder.getFile())

                if (fileViewMode == FileViewMode.LIST) {

                    itemFolder.folderFileTitleViewModel.isSelectMode.set(isSelectMode)
                    itemFolder.folderFileTitleViewModel.isSelected.set(isSelected)

                } else {

                    val view = holder?.viewDataBinding?.root

                    view?.fileTypeImageView?.visibility = if (isSelected) View.INVISIBLE else View.VISIBLE

                    view?.select_file_icon_bg?.visibility = if (isSelectMode) View.VISIBLE else View.INVISIBLE
                    view?.select_file_icon_bg?.setBackgroundResource(
                            if (isSelected) R.drawable.item_selected_state
                            else R.drawable.round_circle)

                }

                itemFolder.folderFileTitleViewModel.init()

                holder?.viewDataBinding?.setVariable(BR.fileItemViewModel, itemFolder.folderFileTitleViewModel)

                val rootView = holder?.viewDataBinding?.root

                rootView?.setOnClickListener {

                    handleItemOnClick(itemFolder.getFile(), position)

                }

                rootView?.setOnLongClickListener {

                    handleItemOnLongClick(itemFolder.getFile())

                    return@setOnLongClickListener true

                }

            }

        }

        holder?.viewDataBinding?.executePendingBindings()

    }

    override fun getItemViewType(position: Int): Int {

        val type = mItemList[position].type

        return if (type == ITEM_FOLDER) {
            if (fileViewMode == FileViewMode.GRID)
                GRID_ITEM_FOLDER
            else
                LIST_ITEM_FOLDER
        } else if (type == ITEM_FILE) {
            if (fileViewMode == FileViewMode.GRID)
                GRID_ITEM_FILE
            else
                LIST_ITEM_FILE
        } else {
            type
        }

    }

    fun getSpanSize(position: Int): Int {

        val type = mItemList[position].type

        return if (type == ITEM_FOLDER_HEAD || type == ITEM_FILE_HEAD)
            SPAN_COUNT
        else
            1

    }

}


private const val ITEM_FOLDER_HEAD = 1
private const val ITEM_FILE_HEAD = 3

private const val ITEM_FOLDER = 2
private const val ITEM_FILE = 4

private const val GRID_ITEM_FOLDER = 5
private const val GRID_ITEM_FILE = 6
private const val LIST_ITEM_FOLDER = 7
private const val LIST_ITEM_FILE = 8

const val ORIENTATION_LIST_TYPE = 0
const val ORIENTATION_GRID_TYPE = 1


open class ItemFolderHead(val folderFileTitleViewModel: FolderFileTitleViewModel) : ViewItem {

    init {
        folderFileTitleViewModel.isFolder.set(true)
    }

    override fun getType(): Int {
        return ITEM_FOLDER_HEAD
    }

}

abstract class ItemContent : ViewItem {

    abstract fun getFile(): AbstractFile

    fun getFileName(): String {
        return getFile().name
    }

    fun getFileSize(): Long {
        return getFile().size
    }

    fun getFileModifyTime(): Long {
        return getFile().time
    }

}

class ItemFolder(val folderFileTitleViewModel: FolderItemViewModel) : ItemContent() {
    override fun getType(): Int {
        return ITEM_FOLDER
    }

    override fun getFile(): AbstractFile {
        return folderFileTitleViewModel.abstractFile
    }

}

class ItemFileHead(folderFileTitleViewModel: FolderFileTitleViewModel) : ItemFolderHead(folderFileTitleViewModel) {

    init {
        folderFileTitleViewModel.isFolder.set(false)
    }

    override fun getType(): Int {
        return ITEM_FILE_HEAD
    }

}

class ItemFile(val fileItemViewModel: FileItemViewModel) : ItemContent() {
    override fun getType(): Int {
        return ITEM_FILE
    }

    override fun getFile(): AbstractFile {
        return fileItemViewModel.abstractFile
    }


}



