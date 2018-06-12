package com.winsun.fruitmix.newdesign201804.file.move

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
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel


const val FILE_OPERATE_KEY = "file_operate_key"

const val FILE_OPERATE_MOVE = 0x1001
const val FILE_OPERATE_COPY = 0x1002



class MoveFileActivity : BaseToolbarActivity(),MoveFileView {

    companion object {

        fun start(context:Context,selectedFiles:MutableList<AbstractFile>,fileOperateKey:Int){

            SelectMoveFileDataSource.saveSelectFiles(selectedFiles)

            val intent = Intent(context, MoveFileActivity::class.java)
            intent.putExtra(FILE_OPERATE_KEY, fileOperateKey)
            context.startActivity(intent)

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

        activityMoveFileBinding.cancelBtn.setOnClickListener {
            cancelMove()
        }

        moveFilePresenter = MoveFilePresenter(InjectFileDataSource.inject(this), activityMoveFileBinding,
                toolbarViewModel, this,loadingViewModel,noContentViewModel,fileOperation)

        moveFilePresenter.initView()

    }

    override fun generateContent(root: ViewGroup?): View {

        activityMoveFileBinding = ActivityMoveFileBinding.inflate(LayoutInflater.from(this), root, false)

        return activityMoveFileBinding.root

    }

    override fun getToolbarTitle(): String {

        return if (fileOperation == FILE_OPERATE_MOVE)
            getString(R.string.move_to)
        else
            getString(R.string.copy_to)
    }

    override fun getRootTitleText():String {
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
