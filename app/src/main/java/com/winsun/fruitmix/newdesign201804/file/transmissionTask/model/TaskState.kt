package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.winsun.fruitmix.util.FileUtil

abstract class TaskState(val task: Task) {

    abstract fun start()

    abstract fun pause()

    abstract fun resume()

    abstract fun cancel()

    abstract fun restart()

    open fun onStartState() {}
    open fun onFinishState() {}

    abstract fun getType(): Int

}

class InitialTaskState(task: Task) : TaskState(task) {

    override fun start() {

        val startTaskState = StartTaskState(task)

        task.setCurrentState(startTaskState)

        startTaskState.start()

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun cancel() {

    }

    override fun restart() {

    }

    override fun getType(): Int {
        return 0
    }

}

class StartTaskState(task: Task) : TaskState(task) {

    override fun start() {

        //TODO: check task amount is enough

        task.executeTask()

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun cancel() {

    }

    override fun restart() {

    }

    override fun getType(): Int {
        return 1
    }
}

private const val UPDATE_SPEED = 0x1001

private const val DELAY_TIME = 1000L

class StartingTaskState(var progress: Int, var speed: String, task: Task) : TaskState(task) {

    var currentHandledSize = 0L

    var lastDownloadSize = 0L

    private lateinit var speedHandler: SpeedHandler

    override fun onStartState() {
        super.onStartState()

        speedHandler = SpeedHandler(Looper.getMainLooper(), this)

        speedHandler.sendEmptyMessageDelayed(UPDATE_SPEED, DELAY_TIME)

    }

    override fun onFinishState() {
        super.onFinishState()

        speedHandler.removeMessages(UPDATE_SPEED)

    }

    override fun start() {

    }

    override fun pause() {

        task.setCurrentState(PauseTaskState(progress, speed, task))

        onFinishState()

    }

    override fun resume() {
    }

    override fun cancel() {

        task.cancelTask()

        onFinishState()

    }

    override fun restart() {

    }

    fun setCurrentHandleFileSize(currentHandledSize: Long) {

        this.currentHandledSize = currentHandledSize

        if (currentHandledSize == 0L && task.abstractFile.size == 0L)
            progress = task.max

        val currentProgress = (currentHandledSize * task.max / task.abstractFile.size).toFloat()

        progress = currentProgress.toInt()

    }

    override fun getType(): Int {
        return 2
    }

}

class SpeedHandler(looper: Looper, val taskState: StartingTaskState) : Handler(looper) {

    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)

        when (msg?.what) {

            UPDATE_SPEED -> {

                val speedSize = taskState.currentHandledSize - taskState.lastDownloadSize

                taskState.speed = FileUtil.formatFileSize(speedSize) + "/s"

                taskState.lastDownloadSize = taskState.currentHandledSize

                taskState.task.setCurrentState(taskState)

                sendEmptyMessageDelayed(UPDATE_SPEED, DELAY_TIME)

            }

        }

    }

}

class PauseTaskState(var progress: Int, var speed: String, task: Task) : TaskState(task) {

    override fun start() {

        task.setCurrentState(StartingTaskState(progress, speed, task))

    }


    override fun pause() {

    }

    override fun resume() {

        task.setCurrentState(StartingTaskState(progress, speed, task))

    }

    override fun cancel() {

    }

    override fun restart() {

    }

    override fun getType(): Int {
        return 3
    }

}

private const val FINISH_TASK_TAG = "finish_task_tag"

class FinishTaskState(task: Task) : TaskState(task) {

    override fun onStartState() {
        super.onStartState()

        Log.d(FINISH_TASK_TAG, "onStartState")
    }

    override fun start() {

    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun cancel() {
    }

    override fun restart() {
    }

    override fun getType(): Int {
        return 4
    }

}

class ErrorTaskState(task: Task) : TaskState(task) {
    override fun start() {

    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun cancel() {
    }

    override fun restart() {
        task.setCurrentState(StartTaskState(task))
    }

    override fun getType(): Int {
        return 5
    }

}




