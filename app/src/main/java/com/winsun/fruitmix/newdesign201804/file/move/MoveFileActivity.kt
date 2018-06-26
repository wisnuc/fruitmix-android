package com.winsun.fruitmix.newdesign201804.file.move

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.databinding.ActivityMoveFileBinding
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.newdesign201804.file.list.data.InjectFileDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.InjectTransmissionTaskDataSource
import com.winsun.fruitmix.newdesign201804.user.preference.InjectUserPreference
import com.winsun.fruitmix.newdesign201804.user.preference.UserPreferenceContainer
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel


const val FILE_OPERATE_KEY = "file_operate_key"

const val FILE_OPERATE_MOVE = 0x1001
const val FILE_OPERATE_COPY = 0x1002
const val FILE_OPERATE_SHARE_TO_SHARED_FOLDER = 0x1003

const val FILE_MOVE_REQUEST_CODE = 0x1002
const val FILE_COPY_REQUEST_CODE = 0x1003
const val FILE_SHARE_TO_SHARED_FOLDER_REQUEST_CODE = 0x1004

class MoveFileActivity : BaseToolbarActivity(), MoveFileView {

    companion object {

        fun start(activity: Activity, selectedFiles: MutableList<AbstractFile>, fileOperateKey: Int) {

            SelectMoveFileDataSource.saveSelectFiles(selectedFiles)

            val intent = Intent(activity, MoveFileActivity::class.java)
            intent.putExtra(FILE_OPERATE_KEY, fileOperateKey)

            when (fileOperateKey) {
                FILE_OPERATE_SHARE_TO_SHARED_FOLDER -> activity.startActivityForResult(intent, FILE_SHARE_TO_SHARED_FOLDER_REQUEST_CODE)
                FILE_OPERATE_COPY -> activity.startActivityForResult(intent, FILE_COPY_REQUEST_CODE)
                FILE_OPERATE_MOVE -> activity.startActivityForResult(intent, FILE_MOVE_REQUEST_CODE)
            }

            selectedFiles.clear()

        }

    }

    private lateinit var activityMoveFileBinding: ActivityMoveFileBinding

    private lateinit var moveFilePresenter: MoveFilePresenter

    private var fileOperation = FILE_OPERATE_MOVE

    override fun onCreate(savedInstanceState: Bundle?) {

        fileOperation = intent.getIntExtra(FILE_OPERATE_KEY, FILE_OPERATE_MOVE)

        super.onCreate(savedInstanceState)

        val noContentViewModel = NoContentViewModel()

        noContentViewModel.noContentImgResId = R.drawable.no_file
        noContentViewModel.setNoContentText(getString(R.string.no_files))

        activityMoveFileBinding.noContentViewModel = noContentViewModel

        val loadingViewModel = LoadingViewModel(this)

        activityMoveFileBinding.loadingViewModel = loadingViewModel

        toolbarViewModel.showMenu.set(true)
        toolbarViewModel.menuResID.set(R.drawable.new_folder)

        toolbarViewModel.setToolbarMenuBtnOnClickListener {

            moveFilePresenter.handleCreateFolderBtnOnClick()

        }

        activityMoveFileBinding.cancelBtn.setOnClickListener {
            cancelMove()
        }

        moveFilePresenter = MoveFilePresenter(InjectFileDataSource.inject(this),
                InjectTransmissionTaskDataSource.provideInstance(this), activityMoveFileBinding,
                toolbarViewModel, this, loadingViewModel, noContentViewModel, fileOperation,
                UserPreferenceContainer.userPreference,InjectUserPreference.inject(this))

        moveFilePresenter.initView()

    }

    override fun generateContent(root: ViewGroup?): View {

        activityMoveFileBinding = ActivityMoveFileBinding.inflate(LayoutInflater.from(this), root, false)

        return activityMoveFileBinding.root

    }

    override fun getToolbarTitle(): String {

        return when (fileOperation) {
            FILE_OPERATE_MOVE -> getString(R.string.move_to)
            FILE_OPERATE_COPY -> getString(R.string.copy_to)
            else -> getString(R.string.share_to_shared_folder)
        }
    }

    override fun getRootTitleText(): String {
        return toolbarTitle
    }

    private fun cancelMove() {
        finish()
    }

    override fun onBackPressed() {

        if (moveFilePresenter.notRoot())
            moveFilePresenter.onBackPressed()
        else
            super.onBackPressed()

    }

}
