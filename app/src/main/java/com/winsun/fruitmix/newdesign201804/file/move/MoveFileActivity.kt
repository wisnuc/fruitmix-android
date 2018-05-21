package com.winsun.fruitmix.newdesign201804.file.move

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.databinding.ActivityMoveFileBinding
import com.winsun.fruitmix.newdesign201804.file.detail.FILE_UUID_KEY
import com.winsun.fruitmix.newdesign201804.file.list.data.InjectFileDataSource

class MoveFileActivity : BaseToolbarActivity() {

    private lateinit var activityMoveFileBinding: ActivityMoveFileBinding

    private lateinit var moveFilePresenter: MoveFilePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbarViewModel.showMenu.set(true)
        toolbarViewModel.menuResID.set(R.drawable.new_folder)

        activityMoveFileBinding.cancelBtn.setOnClickListener {
            cancelMove()
        }

        moveFilePresenter = MoveFilePresenter(InjectFileDataSource.inject(this), activityMoveFileBinding,
                toolbarViewModel)

        moveFilePresenter.initView()

    }

    override fun generateContent(root: ViewGroup?): View {

        activityMoveFileBinding = ActivityMoveFileBinding.inflate(LayoutInflater.from(this), root, false)

        return activityMoveFileBinding.root

    }

    override fun getToolbarTitle(): String {
        return getString(R.string.move_to)
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
