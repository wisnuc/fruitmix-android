package com.winsun.fruitmix.newdesign201804.component

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.*
import kotlinx.android.synthetic.main.task_state_item.view.*

private const val TAG = "TaskStateIcon"

class TaskStateIcon(context: Context?, val attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private var roundProgressBar: RoundProgressBar

    private var taskStateImageView: ImageView

    private var speedTextView: TextView

    init {

        val view = LayoutInflater.from(context).inflate(R.layout.task_state_item, this, true)

        roundProgressBar = view.roundProgressBar

        taskStateImageView = view.taskStateImageView

        speedTextView = view.speedTextView

    }

    fun refresh(taskState: TaskState) {

        Log.d(TAG, "taskState:$taskState")

        if (taskState is StartingTaskState) {

            val progress = taskState.progress
            val speed = taskState.speed

            Log.d(TAG, "set progress,progress: $progress speed: $speed")

            speedTextView.visibility = View.VISIBLE
            speedTextView.text = taskState.speed

            taskStateImageView.visibility = View.INVISIBLE

            roundProgressBar.visibility = View.VISIBLE

            roundProgressBar.circleColor = ContextCompat.getColor(context, R.color.round_progress_color)
            roundProgressBar.circleProgressColor = ContextCompat.getColor(context, R.color.new_design_primary_color)

            roundProgressBar.setProgress(taskState.progress, taskState.speed)

        } else if (taskState is PauseTaskState) {

            speedTextView.visibility = View.INVISIBLE

            taskStateImageView.visibility = View.VISIBLE
            taskStateImageView.setImageResource(R.drawable.pause_download_task)

            roundProgressBar.visibility = View.VISIBLE

            roundProgressBar.circleColor = ContextCompat.getColor(context, R.color.twelve_percent_black)
            roundProgressBar.circleProgressColor = ContextCompat.getColor(context, R.color.twenty_six_percent_black)

            roundProgressBar.setProgress(taskState.progress, taskState.speed)

        } else if (taskState is FinishTaskState) {

            speedTextView.visibility = View.INVISIBLE
            roundProgressBar.visibility = View.INVISIBLE

            taskStateImageView.visibility = View.VISIBLE
            taskStateImageView.setImageResource(R.drawable.task_finish_state)

        } else if (taskState is ErrorTaskState) {

            speedTextView.visibility = View.INVISIBLE
            roundProgressBar.visibility = View.INVISIBLE

            taskStateImageView.visibility = View.VISIBLE
            taskStateImageView.setImageResource(R.drawable.task_error_state)

        }

    }

}