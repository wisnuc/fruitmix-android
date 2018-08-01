package com.winsun.fruitmix.newdesign201804.search

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.databinding.ActivitySearchBinding
import com.winsun.fruitmix.file.data.model.*
import com.winsun.fruitmix.interfaces.BaseView
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.util.inflateView
import com.winsun.fruitmix.newdesign201804.file.list.presenter.*
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FileItemViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderItemViewModel
import com.winsun.fruitmix.newdesign201804.file.operation.FileOperation
import com.winsun.fruitmix.newdesign201804.file.operation.FileOperationView
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.InjectTransmissionTaskRepository
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.TransmissionTaskRepository
import com.winsun.fruitmix.newdesign201804.search.data.SearchDataSource
import com.winsun.fruitmix.newdesign201804.search.data.SearchOrder
import com.winsun.fruitmix.newdesign201804.user.preference.FileViewMode
import com.winsun.fruitmix.newdesign201804.user.preference.UserPreferenceContainer
import com.winsun.fruitmix.newdesign201804.util.getCurrentUserUUID
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel
import com.winsun.fruitmix.viewmodel.ToolbarViewModel

import kotlinx.android.synthetic.main.search_type_item.view.*
import kotlinx.android.synthetic.main.search_type_card.view.*


class SearchPresenter(private val activitySearchBinding: ActivitySearchBinding, val toolbarViewModel: ToolbarViewModel,
                      val loadingViewModel: LoadingViewModel, val noContentViewModel: NoContentViewModel,
                      val searchDataSource: SearchDataSource, val threadManager: ThreadManager,
                      val transmissionTaskRepository: TransmissionTaskRepository, val fileDataSource: FileDataSource,
                      val baseView: BaseView, val fileOperationView: FileOperationView,
                      val searchPlaces: List<String>) {

    private val editText = activitySearchBinding.toolbar?.title!!

    private val fileTypeTitleTv = activitySearchBinding.fileTypeTitleTv

    private val selectedSearchTypeItemRecyclerView = activitySearchBinding.selectedSearchTypeItemRecyclerView

    private val searchTypeItemRecyclerView = activitySearchBinding.searchTypeItemRecyclerView

    private val searchedFileRecyclerView = activitySearchBinding.searchedFileRecyclerView

    private val context = editText.context

    private val selectedSearchTypeItems = mutableListOf<SearchTypeItem>()

    private lateinit var searchTypeAdapter: SearchTypeAdapter

    private lateinit var searchTypeCardViewAdapter: SearchTypeCardViewAdpater

    private lateinit var fileRecyclerViewAdapter: FileRecyclerViewAdapter

    private var currentInputText = ""

    private lateinit var fileOperation: FileOperation

    private val viewItems = mutableListOf<ViewItem>()

    fun initView() {

        editText.clearFocus()

        toolbarViewModel.setToolbarMenuBtnOnClickListener {

            editText.text.clear()

            currentInputText = ""

            handleSelectedSearchTypeItemCountOrInputTextChanged()

        }

/*        editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

                currentInputText = editText.text.toString()

                handleSelectedSearchTypeItemCountOrInputTextChanged()

            }

        })*/

        val searchBtn = activitySearchBinding.toolbar?.searchBtn!!

        searchBtn.setOnClickListener {

            currentInputText = editText.text.toString()

            handleSelectedSearchTypeItemCountOrInputTextChanged()

        }

        initSearchTypeItem()

        showSearchTypeItemRecyclerView()

    }

    private fun initSearchTypeItem() {

        initFileRecyclerView()

        searchTypeCardViewAdapter = SearchTypeCardViewAdpater {

            selectedSearchTypeItems.remove(it)

            handleSelectedSearchTypeItemCountOrInputTextChanged()

        }

        selectedSearchTypeItemRecyclerView.layoutManager = LinearLayoutManager(context, OrientationHelper.HORIZONTAL, false)

        selectedSearchTypeItemRecyclerView.adapter = searchTypeCardViewAdapter

        val searchTypeItems = listOf<SearchTypeItem>(

                SearchTypeItem(R.drawable.pdf, "PDFs"),
                SearchTypeItem(R.drawable.word, "Word"),
                SearchTypeItem(R.drawable.excel, "Excel"),
                SearchTypeItem(R.drawable.power_point, "PPT"),
                SearchTypeItem(R.drawable.photo_image, "Photos&Images"),
                SearchTypeItem(R.drawable.video, "Video"),
                SearchTypeItem(R.drawable.audio, "Audio")

        )

        searchTypeAdapter = SearchTypeAdapter {

            selectedSearchTypeItems.add(it)

            handleSelectedSearchTypeItemCountOrInputTextChanged()

        }

        searchTypeAdapter.setItemList(searchTypeItems)

        searchTypeItemRecyclerView.layoutManager = LinearLayoutManager(context)

        searchTypeItemRecyclerView.adapter = searchTypeAdapter

        searchTypeAdapter.notifyDataSetChanged()

    }

    private fun initFileRecyclerView() {

        fileOperation = FileOperation(context.getCurrentUserUUID(), threadManager, transmissionTaskRepository,
                editText, fileDataSource, baseView, fileOperationView,
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

        searchedFileRecyclerView.layoutManager = LinearLayoutManager(context)

        fileRecyclerViewAdapter = FileRecyclerViewAdapter(
                { abstractFile, position ->
                    handleItemOnClick(abstractFile, position)
                },
                {
                    return@FileRecyclerViewAdapter false
                },
                { abstractFile, position ->
                    fileOperation.doShowFileMenuBottomDialog(context, abstractFile, position)
                },
                sortPolicy = UserPreferenceContainer.userPreference.fileSortPolicy)

        fileRecyclerViewAdapter.fileViewMode = FileViewMode.LIST

        searchedFileRecyclerView.adapter = fileRecyclerViewAdapter

    }

    private fun handleItemOnClick(abstractFile: AbstractFile, position: Int) {

        if (abstractFile is RemoteFile)
            fileOperation.openFileAfterOnClick(abstractFile)

    }

    private fun handleSelectedSearchTypeItemCountOrInputTextChanged() {

        if (currentInputText.isEmpty() && selectedSearchTypeItems.isEmpty()) {

            toolbarViewModel.showMenu.set(false)

            loadingViewModel.showLoading.set(false)
            noContentViewModel.showNoContent.set(false)

            selectedSearchTypeItemRecyclerView.visibility = View.GONE
            fileTypeTitleTv.visibility = View.VISIBLE

            searchedFileRecyclerView.visibility = View.INVISIBLE

            searchTypeItemRecyclerView.visibility = View.VISIBLE

        } else {

            fileTypeTitleTv.visibility = View.GONE

            searchTypeItemRecyclerView.visibility = View.INVISIBLE

            if (currentInputText.isNotEmpty())
                toolbarViewModel.showMenu.set(true)

            if (selectedSearchTypeItems.isNotEmpty()) {

                selectedSearchTypeItemRecyclerView.visibility = View.VISIBLE

                searchTypeCardViewAdapter.setItemList(selectedSearchTypeItems)
                searchTypeCardViewAdapter.notifyDataSetChanged()

            } else {

                selectedSearchTypeItemRecyclerView.visibility = View.GONE

            }

            searchedFileRecyclerView.visibility = View.VISIBLE

            startSearch()

        }

    }

    private fun startSearch() {

        loadingViewModel.showLoading.set(true)

        val classesBuilder = StringBuilder()
        val typesBuilder = StringBuilder()

        selectedSearchTypeItems.forEach {

            when (it.imageResID) {
                R.drawable.photo_image -> classesBuilder.append("image,")
                R.drawable.word -> typesBuilder.append("DOC.")
                R.drawable.pdf -> typesBuilder.append("PDF.")
                R.drawable.power_point -> typesBuilder.append("PPT.")
                R.drawable.excel -> typesBuilder.append("XLSX.")
                R.drawable.video -> classesBuilder.append("video,")
                R.drawable.audio -> classesBuilder.append("audio,")
            }

        }

        var classes = classesBuilder.toString()
        var types = typesBuilder.toString()

        if (classes.isNotEmpty()) {
            val classesLength = classes.length
            classes = classes.substring(0, classesLength - 1)
        }

        if (types.isNotEmpty()) {
            val typesLength = types.length
            types = types.substring(0, typesLength - 1)
        }

        var searchOrder = SearchOrder.NUll

        if (currentInputText.isNotEmpty() && classes.isEmpty() && types.isEmpty())
            searchOrder = SearchOrder.FIND

        searchDataSource.searchFile(name = if (currentInputText.isNotEmpty()) currentInputText else "", places = searchPlaces,
                types = if (types.isNotEmpty()) types else "", searchClasses = if (classes.isNotEmpty()) classes else "",
                searchOrder = searchOrder,
                baseLoadDataCallback = object : BaseLoadDataCallback<AbstractRemoteFile> {

                    override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                        if (data!!.isEmpty()) {

                            loadingViewModel.showLoading.set(false)
                            noContentViewModel.showNoContent.set(true)

                        } else {

                            loadingViewModel.showLoading.set(false)
                            noContentViewModel.showNoContent.set(false)

                            handleSearchSucceed(data)

                        }

                    }

                    override fun onFail(operationResult: OperationResult?) {
                        loadingViewModel.showLoading.set(false)
                        noContentViewModel.showNoContent.set(true)
                    }

                })

    }

    private fun handleSearchSucceed(abstractFiles: MutableList<AbstractRemoteFile>) {

        val folderViewItems = mutableListOf<ViewItem>()
        val fileViewItems = mutableListOf<ViewItem>()

        viewItems.clear()

        abstractFiles.forEach {

            if (it.isFolder) {

                val folderItemViewModel = FolderItemViewModel(it as RemoteFolder)
                folderItemViewModel.showOfflineAvailableIv.set(true)

                folderViewItems.add(ItemFolder(folderItemViewModel))
            } else {

                val fileItemViewModel = FileItemViewModel(it as RemoteFile)
                fileItemViewModel.showOfflineAvailableIv.set(true)

                fileViewItems.add(ItemFile(fileItemViewModel))
            }

        }

        viewItems.addAll(folderViewItems)
        viewItems.addAll(fileViewItems)

        fileRecyclerViewAdapter.setItemList(viewItems)
        fileRecyclerViewAdapter.notifyDataSetChanged()

    }

    private fun showSearchTypeItemRecyclerView() {

        toolbarViewModel.showMenu.set(false)

        editText.text.clear()

        handleSelectedSearchTypeItemCountOrInputTextChanged()

    }

}


data class SearchTypeItem(val imageResID: Int, val text: String)

class SearchTypeAdapter(val itemOnClick: (searchTypeItem: SearchTypeItem) -> Unit) : BaseRecyclerViewAdapter<SimpleViewHolder, SearchTypeItem>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

        val view = parent?.inflateView(R.layout.search_type_item)

        return SimpleViewHolder(view)

    }

    override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

        val view = holder?.itemView

        val searchTypeItem = mItemList[position]

        view?.typeImageView?.setImageResource(searchTypeItem.imageResID)
        view?.typeTextView?.text = searchTypeItem.text

        view?.setOnClickListener {
            itemOnClick(searchTypeItem)
        }

    }

}

class SearchTypeCardViewAdpater(val deleteTypeTvOnClick: (searchTypeItem: SearchTypeItem) -> Unit) : BaseRecyclerViewAdapter<SimpleViewHolder, SearchTypeItem>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

        val view = parent?.inflateView(R.layout.search_type_card)

        return SimpleViewHolder(view)

    }

    override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

        val view = holder?.itemView

        val searchTypeItem = mItemList[position]

        view?.cardTypeImageView?.setImageResource(searchTypeItem.imageResID)
        view?.cardTypeTextView?.text = searchTypeItem.text

        view?.deleteTypeIv?.setOnClickListener {
            deleteTypeTvOnClick(searchTypeItem)
        }


    }

}
