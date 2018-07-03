package com.winsun.fruitmix.newdesign201804.file.transmissionTask

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.command.BaseAbstractCommand
import com.winsun.fruitmix.databinding.ActivityTransmissionTaskBinding
import com.winsun.fruitmix.dialog.BottomMenuListDialogFactory
import com.winsun.fruitmix.model.BottomMenuItem
import com.winsun.fruitmix.newdesign201804.component.getCurrentUserUUID
import com.winsun.fruitmix.newdesign201804.component.inflateView
import com.winsun.fruitmix.newdesign201804.file.transmission.InjectTransmissionDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.InjectTransmissionTaskRepository
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel
import kotlinx.android.synthetic.main.activity_transmission_task.*

class TransmissionTaskActivity : BaseToolbarActivity() {

    private lateinit var transmissionTaskPresenter: TransmissionTaskPresenter

    private lateinit var activityTransmissionTaskBinding: ActivityTransmissionTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbarViewModel.menuResID.set(R.drawable.horizontal_more_icon_black)

        toolbarViewModel.showMenu.set(true)
        toolbarViewModel.setToolbarMenuBtnOnClickListener {

            showBottomDialog()

        }

        val loadingViewModel = LoadingViewModel(this)

        activityTransmissionTaskBinding.loadingViewModel = loadingViewModel

        val noContentViewModel = NoContentViewModel()
        noContentViewModel.noContentImgResId = R.drawable.no_file
        noContentViewModel.setNoContentText(getString(R.string.no_task))

        activityTransmissionTaskBinding.noContentViewModel = noContentViewModel

        transmissionTaskPresenter = TransmissionTaskPresenter(InjectTransmissionTaskRepository.provideInstance(this),
                InjectTransmissionDataSource.inject(this),
                loadingViewModel, noContentViewModel, getCurrentUserUUID())

        recyclerView.layoutManager = LinearLayoutManager(this)

        transmissionTaskPresenter.initView(recyclerView)

    }

    override fun generateContent(root: ViewGroup?): View {

        activityTransmissionTaskBinding = ActivityTransmissionTaskBinding.inflate(LayoutInflater.from(root?.context),
                root, false)

        return activityTransmissionTaskBinding.root

    }

    override fun getToolbarTitle(): String {
        return getString(R.string.transmission_task)
    }


    private fun showBottomDialog() {

        val bottomMenuItems = listOf<BottomMenuItem>(

                BottomMenuItem(0, getString(R.string.start_all_task), object : BaseAbstractCommand() {

                    override fun execute() {
                        super.execute()

                        transmissionTaskPresenter.startAllTask()
                    }

                }),
                BottomMenuItem(0, getString(R.string.pause_all_task), object : BaseAbstractCommand() {

                    override fun execute() {
                        super.execute()

                        transmissionTaskPresenter.pauseAllTask()
                    }

                }),
                BottomMenuItem(0, getString(R.string.clear_completed_task), object : BaseAbstractCommand() {

                    override fun execute() {
                        super.execute()

                        transmissionTaskPresenter.clearAllFinishedTask()
                    }

                })

        )

        BottomMenuListDialogFactory(bottomMenuItems).createDialog(this).show()

    }

    override fun onDestroy() {
        super.onDestroy()

        transmissionTaskPresenter.onDestroy()

    }

}
