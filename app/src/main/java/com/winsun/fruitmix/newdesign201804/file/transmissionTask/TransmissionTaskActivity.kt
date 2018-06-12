package com.winsun.fruitmix.newdesign201804.file.transmissionTask

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.command.BaseAbstractCommand
import com.winsun.fruitmix.dialog.BottomMenuListDialogFactory
import com.winsun.fruitmix.model.BottomMenuItem
import com.winsun.fruitmix.newdesign201804.component.inflateView
import com.winsun.fruitmix.newdesign201804.file.transmission.InjectTransmissionDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.InjectTransmissionTaskDataSource
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl
import kotlinx.android.synthetic.main.activity_transmission_task.*

class TransmissionTaskActivity : BaseToolbarActivity() {

    private lateinit var transmissionTaskPresenter: TransmissionTaskPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbarViewModel.menuResID.set(R.drawable.horizontal_more_icon_black)

        toolbarViewModel.showMenu.set(true)
        toolbarViewModel.setToolbarMenuBtnOnClickListener {

            showBottomDialog()

        }

        transmissionTaskPresenter = TransmissionTaskPresenter(InjectTransmissionTaskDataSource.provideInstance(this),
                InjectTransmissionDataSource.inject(this))

        recyclerView.layoutManager = LinearLayoutManager(this)

        transmissionTaskPresenter.initView(recyclerView)

    }

    override fun generateContent(root: ViewGroup?): View {

        return root?.inflateView(R.layout.activity_transmission_task)!!

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
