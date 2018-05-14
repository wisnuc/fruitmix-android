package com.winsun.fruitmix.newdesign201804.file.list.presenter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BR
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.command.BaseAbstractCommand
import com.winsun.fruitmix.databinding.*
import com.winsun.fruitmix.dialog.BottomMenuGridDialogFactory
import com.winsun.fruitmix.dialog.BottomMenuListDialogFactory
import com.winsun.fruitmix.dialog.FileMenuBottomDialogFactory
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.model.BottomMenuItem
import com.winsun.fruitmix.model.DivideBottomMenuItem
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.component.FileSelectModeTitle
import com.winsun.fruitmix.newdesign201804.file.detail.FILE_UUID_KEY
import com.winsun.fruitmix.newdesign201804.file.detail.FileDetailActivity
import com.winsun.fruitmix.newdesign201804.file.list.MainPageDividerItemDecoration
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FileItemViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderFileTitleViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderItemViewModel
import com.winsun.fruitmix.newdesign201804.file.move.MoveFileActivity
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.BindingViewHolder
import com.winsun.fruitmix.util.FileUtil
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel
import kotlinx.android.synthetic.main.folder_item.view.*
import kotlinx.android.synthetic.main.search_file_card.view.*

private const val SPAN_COUNT = 2

private val mSelectFiles = mutableListOf<AbstractFile>()
private var mIsSelectMode = false

public class FilePresenter(val fileDataSource: FileDataSource, val noContentViewModel: NoContentViewModel,
                           val loadingViewModel: LoadingViewModel, val filePageBinding: FilePageBinding) {

    private val contentLayout = filePageBinding.contentLayout

    private lateinit var fileRecyclerViewAdapter: FileRecyclerViewAdapter

    private var currentOrientation = ORIENTATION_GRID_TYPE

    private val currentFolderItems: MutableList<AbstractRemoteFile> = mutableListOf()

    private val context: Context = filePageBinding.fileRecyclerView.context

    private val gridLayoutManager = GridLayoutManager(context, SPAN_COUNT)

    private val linearLayoutManager = LinearLayoutManager(context)

    private val viewItems = mutableListOf<ViewItem>()

    private lateinit var fileSelectModeTitle: FileSelectModeTitle

    private lateinit var mainPageDividerItemDecoration: MainPageDividerItemDecoration

    fun initView() {

        fileRecyclerViewAdapter = FileRecyclerViewAdapter({ remoteFile, position ->
            doHandleItemOnClick(remoteFile, position)
        }, {
            doHandleOnLongClick(it)
        }, {
            doShowFileMenuBottomDialog(context, it)
        })

        fileSelectModeTitle = FileSelectModeTitle(filePageBinding.fileSelectModeTitle!!,
                { quitSelectMode() },
                { enterMovePage() },
                {},
                {})

        gridLayoutManager.spanSizeLookup.isSpanIndexCacheEnabled = true

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return fileRecyclerViewAdapter.getSpanSize(position)
            }
        }

        loadingViewModel.showLoading.set(true)

        fileDataSource.getFile(object : BaseLoadDataCallback<AbstractRemoteFile> {

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                loadingViewModel.showLoading.set(false)
                noContentViewModel.showNoContent.set(false)

                currentFolderItems.addAll(data!!)

                initRecyclerView()

                initToggleOrientationIv()

            }

            override fun onFail(operationResult: OperationResult?) {

                loadingViewModel.showLoading.set(false)
                noContentViewModel.showNoContent.set(true)

            }

        })

    }

    private fun initRecyclerView() {

        fileRecyclerViewAdapter.currentOrientation = currentOrientation

        val folderViewItems = mutableListOf<ViewItem>()
        val fileViewItems = mutableListOf<ViewItem>()

        currentFolderItems.forEach {

            if (it.isFolder) {
                folderViewItems.add(ItemFolder(FolderItemViewModel(it as RemoteFolder)))
            } else {
                fileViewItems.add(ItemFile(FileItemViewModel(it as RemoteFile)))
            }

        }

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

        filePageBinding.fileRecyclerView.itemAnimator = DefaultItemAnimator()

        mainPageDividerItemDecoration = MainPageDividerItemDecoration(SPAN_COUNT, viewItems.size)

        setRecyclerViewLayoutManager()

        filePageBinding.fileRecyclerView.adapter = fileRecyclerViewAdapter

        fileRecyclerViewAdapter.setItemList(viewItems)
        fileRecyclerViewAdapter.notifyDataSetChanged()

        filePageBinding.addFab.setOnClickListener {

            showAddDialog()

        }

    }

    private fun showAddDialog() {
        val bottomMenuItems = mutableListOf<BottomMenuItem>()

        val bottomMenuItem = BottomMenuItem(R.drawable.bottom_menu_folder, context.getString(R.string.folder), object : BaseAbstractCommand() {})

        bottomMenuItems.add(bottomMenuItem)

        val uploadBottomMenuItem = BottomMenuItem(R.drawable.upload, context.getString(R.string.upload), object : BaseAbstractCommand() {})

        bottomMenuItems.add(uploadBottomMenuItem)

        val magnetBottomMenuItem = BottomMenuItem(R.drawable.magnet_link, context.getString(R.string.magnet_link), object : BaseAbstractCommand() {})

        bottomMenuItems.add(magnetBottomMenuItem)

        BottomMenuGridDialogFactory(bottomMenuItems).createDialog(context).show()
    }


    private fun initToggleOrientationIv() {

        val view = filePageBinding.searchFileCard

        view?.toggleOrientationIv?.setOnClickListener {

            toggleOrientation()

        }

        view?.moreIv?.setOnClickListener {

            handleMoreIvClick()

        }

    }

    private fun handleMoreIvClick() {

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

    private fun toggleOrientation() {

        currentOrientation = if (currentOrientation == ORIENTATION_GRID_TYPE)
            ORIENTATION_LIST_TYPE
        else
            ORIENTATION_GRID_TYPE

        setRecyclerViewLayoutManager()

        fileRecyclerViewAdapter.currentOrientation = currentOrientation

        fileRecyclerViewAdapter.setItemList(viewItems)
        fileRecyclerViewAdapter.notifyDataSetChanged()

    }

    private fun toggleTitle() {

        if (mIsSelectMode) {
            filePageBinding.fileSelectModeTitle?.visibility = View.VISIBLE
            filePageBinding.searchFileCard?.visibility = View.INVISIBLE
        } else {

            filePageBinding.searchFileCard?.visibility = View.VISIBLE
            filePageBinding.fileSelectModeTitle?.visibility = View.INVISIBLE

        }

    }

    private fun setRecyclerViewLayoutManager() {

        if (currentOrientation == ORIENTATION_GRID_TYPE) {

            filePageBinding.fileRecyclerView.layoutManager = gridLayoutManager

            filePageBinding.fileRecyclerView.addItemDecoration(mainPageDividerItemDecoration)

        } else if (currentOrientation == ORIENTATION_LIST_TYPE) {

            filePageBinding.fileRecyclerView.layoutManager = linearLayoutManager

            filePageBinding.fileRecyclerView.removeItemDecoration(mainPageDividerItemDecoration)

        }

    }

    private fun doHandleOnLongClick(abstractFile: AbstractFile) {

        if (mIsSelectMode)
            return

        mSelectFiles.add(abstractFile)

        fileSelectModeTitle.notifySelectCountChanged(mSelectFiles.size)

        enterSelectMode()

    }


    private fun doHandleItemOnClick(abstractFile: AbstractFile, position: Int) {

        if (!mIsSelectMode)
            return

        if (mSelectFiles.contains(abstractFile))
            mSelectFiles.remove(abstractFile)
        else
            mSelectFiles.add(abstractFile)

        if (mSelectFiles.isEmpty()) {
            quitSelectMode()
        } else {

            fileSelectModeTitle.notifySelectCountChanged(mSelectFiles.size)

            fileRecyclerViewAdapter.selectFiles = mSelectFiles
            fileRecyclerViewAdapter.isSelectMode = mIsSelectMode

            fileRecyclerViewAdapter.notifyItemChanged(position)

        }

    }

    fun useDefaultBackPressFunction(): Boolean {
        return !mIsSelectMode
    }

    fun onBackPressed() {
        quitSelectMode()
    }

    private fun enterSelectMode() {

        mIsSelectMode = true

        fileRecyclerViewAdapter.isSelectMode = mIsSelectMode
        fileRecyclerViewAdapter.selectFiles = mSelectFiles

        fileRecyclerViewAdapter.notifyDataSetChanged()

        toggleTitle()

    }

    private fun quitSelectMode() {

        mSelectFiles.clear()

        mIsSelectMode = false

        fileRecyclerViewAdapter.isSelectMode = mIsSelectMode
        fileRecyclerViewAdapter.selectFiles = mSelectFiles

        fileRecyclerViewAdapter.notifyDataSetChanged()

        toggleTitle()

    }

    private fun doShowFileMenuBottomDialog(context: Context, abstractFile: AbstractFile) {

        val bottomMenuItems = mutableListOf<BottomMenuItem>()

        bottomMenuItems.add(BottomMenuItem(R.drawable.modify_icon, context.getString(R.string.rename), object : BaseAbstractCommand() {}))

        bottomMenuItems.add(BottomMenuItem(R.drawable.black_move, context.getString(R.string.move_to), object : BaseAbstractCommand() {
            override fun execute() {
                super.execute()
                enterMovePage()
            }
        }))

        if (!abstractFile.isFolder) {

            val bottomMenuItem = BottomMenuItem(R.drawable.offline_available, context.getString(R.string.offline_available), object : BaseAbstractCommand() {})
            bottomMenuItem.isShowSwitchBtn = true

            bottomMenuItems.add(bottomMenuItem)

        }

        bottomMenuItems.add(BottomMenuItem(R.drawable.open_with_other_app, context.getString(R.string.open_with_other_app), object : BaseAbstractCommand() {}))

        bottomMenuItems.add(DivideBottomMenuItem())

        if (!abstractFile.isFolder) {

            bottomMenuItems.add(BottomMenuItem(R.drawable.make_a_copy, context.getString(R.string.make_a_copy), object : BaseAbstractCommand() {}))

            bottomMenuItems.add(BottomMenuItem(R.drawable.edit_tag, context.getString(R.string.edit_tag), object : BaseAbstractCommand() {}))

        }

        bottomMenuItems.add(BottomMenuItem(R.drawable.share_to_shared_folder, context.getString(R.string.share_to_shared_folder), object : BaseAbstractCommand() {}))

        bottomMenuItems.add(BottomMenuItem(R.drawable.copy_to, context.getString(R.string.copy_to), object : BaseAbstractCommand() {}))

        bottomMenuItems.add(BottomMenuItem(R.drawable.delete_download_task, context.getString(R.string.delete_text), object : BaseAbstractCommand() {}))

        FileMenuBottomDialogFactory(abstractFile, bottomMenuItems, {

            val intent = Intent(context, FileDetailActivity::class.java)

            val abstractRemoteFile = it as AbstractRemoteFile
            intent.putExtra(FILE_UUID_KEY, abstractRemoteFile.uuid)

            //TODO: check pass uuid

            context.startActivity(intent)

        }).createDialog(context).show()


    }

    private fun enterMovePage() {

        val intent = Intent(context, MoveFileActivity::class.java)
        context.startActivity(intent)

    }


}

class FileRecyclerViewAdapter(val handleItemOnClick: (abstractFile: AbstractFile, position: Int) -> Unit,
                              val handleItemOnLongClick: (abstractFile: AbstractFile) -> Unit,
                              val doHandleMoreBtnOnClick: (abstractFile: AbstractFile) -> Unit) : BaseRecyclerViewAdapter<BindingViewHolder, ViewItem>() {

    var currentOrientation = ORIENTATION_GRID_TYPE

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

                folderFileTitleBinding.folderFileTitleViewModel = itemFolderHead.folderFileTitleViewModel

                val context = folderFileTitleBinding.nameTextView.context

                folderFileTitleBinding.sortLayout.setOnClickListener {
                    showSortBottomDialog(context)
                }

            }
            GRID_ITEM_FILE, LIST_ITEM_FILE -> {

                val rootView = holder?.viewDataBinding?.root

                val itemFile = viewItem as ItemFile

                val fileItemViewModel = FileItemViewModel(itemFile.getFile()) {
                    doHandleMoreBtnOnClick(itemFile.getFile())
                }

                fileItemViewModel.showMoreBtn.set(itemFile.fileItemViewModel.showMoreBtn.get())
                fileItemViewModel.showOfflineAvailableIv.set(itemFile.fileItemViewModel.showOfflineAvailableIv.get())

                fileItemViewModel.isSelectMode.set(isSelectMode)
                fileItemViewModel.isSelected.set(selectFiles.contains(itemFile.getFile()))

                holder?.viewDataBinding?.setVariable(BR.fileItemViewModel, fileItemViewModel)

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

                val folderItemViewModel = FolderItemViewModel(itemFolder.getFile()) {
                    doHandleMoreBtnOnClick(itemFolder.getFile())
                }

                val isSelected = selectFiles.contains(itemFolder.getFile())

                if (currentOrientation == ORIENTATION_LIST_TYPE) {

                    folderItemViewModel.isSelectMode.set(isSelectMode)
                    folderItemViewModel.isSelected.set(isSelected)

                    folderItemViewModel.showMoreBtn.set(itemFolder.folderFileTitleViewModel.showMoreBtn.get())
                    folderItemViewModel.showOfflineAvailableIv.set(itemFolder.folderFileTitleViewModel.showOfflineAvailableIv.get())

                } else {

                    val view = holder?.viewDataBinding?.root

                    view?.fileTypeImageView?.visibility = if (isSelected) View.INVISIBLE else View.VISIBLE

                    view?.select_file_icon_bg?.visibility = if (isSelectMode) View.VISIBLE else View.INVISIBLE
                    view?.select_file_icon_bg?.setBackgroundResource(
                            if (isSelected) R.drawable.item_selected_state
                            else R.drawable.round_circle)


                }

                holder?.viewDataBinding?.setVariable(BR.fileItemViewModel, folderItemViewModel)

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


    private fun showSortBottomDialog(context: Context) {

        val bottomMenuItems = mutableListOf<BottomMenuItem>()

        val bottomMenuItem = BottomMenuItem(R.drawable.sort, context.getString(R.string.sort_by_name), object : BaseAbstractCommand() {

        })
        bottomMenuItem.rightResID = R.drawable.green_done

        bottomMenuItems.add(bottomMenuItem)

        bottomMenuItems.add(BottomMenuItem(0, context.getString(R.string.sort_by_modify_time), object : BaseAbstractCommand() {

        }))

        bottomMenuItems.add(BottomMenuItem(0, context.getString(R.string.sort_by_create_time), object : BaseAbstractCommand() {

        }))

        bottomMenuItems.add(BottomMenuItem(0, context.getString(R.string.sort_by_capacity), object : BaseAbstractCommand() {

        }))

        BottomMenuListDialogFactory(bottomMenuItems).createDialog(context).show()

    }


    override fun getItemViewType(position: Int): Int {

        val type = mItemList[position].type

        return if (type == ITEM_FOLDER) {
            if (currentOrientation == ORIENTATION_GRID_TYPE)
                GRID_ITEM_FOLDER
            else
                LIST_ITEM_FOLDER
        } else if (type == ITEM_FILE) {
            if (currentOrientation == ORIENTATION_GRID_TYPE)
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

class ItemFolder(val folderFileTitleViewModel: FolderItemViewModel) : ViewItem {
    override fun getType(): Int {
        return ITEM_FOLDER
    }

    fun getFile(): AbstractFile {
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

class ItemFile(val fileItemViewModel: FileItemViewModel) : ViewItem {
    override fun getType(): Int {
        return ITEM_FILE
    }

    fun getFile(): AbstractFile {
        return fileItemViewModel.abstractFile
    }

}



