package com.winsun.fruitmix.newdesign201804.file.transmissionTask

import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.file.data.model.RemoteFolder
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.util.inflateView
import com.winsun.fruitmix.newdesign201804.file.transmission.TransmissionDataSource
import com.winsun.fruitmix.newdesign201804.file.transmission.model.Transmission
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.TransmissionTaskDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.*
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl
import com.winsun.fruitmix.util.SnackbarUtil
import com.winsun.fruitmix.util.Util
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel

import kotlinx.android.synthetic.main.transmission_task_item.view.*

class TransmissionTaskPresenter(val transmissionTaskDataSource: TransmissionTaskDataSource,
                                val transmissionDataSource: TransmissionDataSource,
                                val loadingViewModel: LoadingViewModel, val noContentViewModel: NoContentViewModel,
                                val currentUserUUID: String) {

    private val transmissionTaskAdapter = TransmissionTaskAdapter()

    private val taskContainers = mutableListOf<TaskContainer>()

    fun initView(recyclerView: RecyclerView) {

        recyclerView.adapter = transmissionTaskAdapter

        loadingViewModel.showLoading.set(true)

        transmissionTaskDataSource.getAllTransmissionTasks(object : BaseLoadDataCallback<Task> {

            override fun onSucceed(data: MutableList<Task>?, operationResult: OperationResult?) {

                getTransmissionData(data)

            }

            override fun onFail(operationResult: OperationResult?) {

                loadingViewModel.showLoading.set(false)
                noContentViewModel.showNoContent.set(true)

            }

        })


    }

    private fun getTransmissionData(taskData: MutableList<Task>?) {
        transmissionDataSource.getTransmission(object : BaseLoadDataCallback<Transmission> {

            override fun onFail(operationResult: OperationResult?) {

                handleGetTask(taskData)

            }

            override fun onSucceed(data: MutableList<Transmission>?, operationResult: OperationResult?) {

                data?.forEach {

                    val abstractFile = RemoteFolder()
                    abstractFile.uuid = it.dirUUID

                    val btTaskParam = BTTaskParam(it)

                    val task = BTTask(Util.createLocalUUid(), currentUserUUID, abstractFile, ThreadManagerImpl.getInstance(), btTaskParam,
                            transmissionDataSource)

                    taskData?.add(task)

                }

                handleGetTask(taskData)

            }

        })
    }

    fun onDestroy() {

        taskContainers.forEach {
            it.destroy()
        }

    }

    fun startAllTask() {

        taskContainers.forEach {
            it.task.startTask()
        }

    }

    fun pauseAllTask() {

        taskContainers.forEach {
            it.task.pauseTask()
        }

    }

    private var currentDeletedFinishedTaskCount = 0
    private var totalFinishedTaskCount = 0

    fun clearAllFinishedTask() {

        val finishedTaskContainers = taskContainers.filter {
            it.task.getCurrentState().getType() == StateType.FINISH
        }

        totalFinishedTaskCount = finishedTaskContainers.size

        finishedTaskContainers.forEach {

            it.task.deleteTask()

            transmissionTaskDataSource.deleteTransmissionTask(it.task, object : BaseOperateCallback {
                override fun onFail(operationResult: OperationResult?) {
                    handleDeletedFinishedTaskFinish(finishedTaskContainers)
                }

                override fun onSucceed() {
                    handleDeletedFinishedTaskFinish(finishedTaskContainers)
                }
            })
        }

    }

    private fun handleDeletedFinishedTaskFinish(finishedTaskContainers: List<TaskContainer>) {

        currentDeletedFinishedTaskCount++

        if (currentDeletedFinishedTaskCount == totalFinishedTaskCount) {

            taskContainers.removeAll(finishedTaskContainers)

            if (taskContainers.isEmpty()) {
                noContentViewModel.showNoContent.set(true)
                return
            } else
                noContentViewModel.showNoContent.set(false)

            transmissionTaskAdapter.setItemList(taskContainers)
            transmissionTaskAdapter.notifyDataSetChanged()

        }

    }


    private fun handleGetTask(data: MutableList<Task>?) {

        loadingViewModel.showLoading.set(false)

        data?.forEach {

            val taskContainer = TaskContainer(it)

            it.init()

            taskContainers.add(taskContainer)

        }

        if (taskContainers.isEmpty()) {
            noContentViewModel.showNoContent.set(true)
            return
        } else
            noContentViewModel.showNoContent.set(false)

        transmissionTaskAdapter.setItemList(taskContainers)

        transmissionTaskAdapter.notifyDataSetChanged()

        data?.forEach {

            when (it) {
                is SMBTask -> it.setCurrentState(FinishTaskState(it.abstractFile.size, it))
                is BaseMoveCopyTask -> it.setCurrentState(it.getCurrentState())
                else -> it.startTask()
            }

        }

    }

    private inner class TransmissionTaskAdapter : RecyclerSwipeAdapter<SimpleViewHolder>() {

        private val mItemList: MutableList<TaskContainer> = mutableListOf()

        fun setItemList(itemList: List<TaskContainer>) {
            mItemList.clear()
            mItemList.addAll(itemList)
        }

        override fun getSwipeLayoutResourceId(position: Int): Int {
            return R.id.swipeLayout
        }

        override fun getItemCount(): Int {
            return mItemList.size
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            val view = parent?.inflateView(R.layout.transmission_task_item)

            return SimpleViewHolder(view)

        }

        override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

            val view = holder?.itemView

            val context = view?.context

            val taskContainer = mItemList[position]

            val task = taskContainer.task

            view?.taskFileTypeIv?.setImageResource(task.abstractFile.fileTypeResID)

            val taskFileName =
                    if (task is BaseMoveCopyTask) {

                        if (task.taskParam.entries.size > 1)
                            context?.getString(R.string.file_and_so_on, task.abstractFile.name)
                        else
                            task.abstractFile.name
                    } else
                        task.abstractFile.name

            view?.taskFileNameTv?.text = taskFileName

            view?.taskTypeIv?.setImageResource(task.getTypeResID())

            view?.taskStateIcon?.setOnClickListener {

                val taskStateType = task.getCurrentState().getType()

                if (taskStateType == StateType.PAUSE)
                    task.resumeTask()
                else if (taskStateType == StateType.STARTING)
                    task.pauseTask()

            }

            view?.deleteTv?.setOnClickListener {

                task.deleteTask()

                transmissionTaskDataSource.deleteTransmissionTask(task, object : BaseOperateCallback {
                    override fun onSucceed() {

                        taskContainers.remove(taskContainer)

                        transmissionTaskAdapter.setItemList(taskContainers)

                        transmissionTaskAdapter.notifyItemRemoved(holder.adapterPosition)

                    }

                    override fun onFail(operationResult: OperationResult?) {
                        SnackbarUtil.showSnackBar(view, Snackbar.LENGTH_SHORT, messageStr = operationResult!!.getResultMessage(context))
                    }

                })

            }

            view?.taskStateIcon?.refresh(task.getCurrentState())

        }

    }


    private inner class TaskContainer(val task: Task) : TaskStateObserver {

        init {
            task.registerObserver(this)
        }

        fun destroy() {
            task.unregisterObserver(this)
        }

        override fun notifyStateChanged(currentState: TaskState, preState: TaskState) {

            val position = taskContainers.indexOf(this)

            Log.d("Task", "notifyStateChanged position: $position")

            transmissionTaskAdapter.notifyItemChanged(position)

        }


    }

}



