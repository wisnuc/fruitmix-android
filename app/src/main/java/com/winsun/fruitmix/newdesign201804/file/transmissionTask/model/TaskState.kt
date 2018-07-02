package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.winsun.fruitmix.util.FileUtil

enum class StateType(val value:Int) {
    INITIAL(0), START(1), STARTING(2), PAUSE(3), FINISH(4), ERROR(5)
}

abstract class TaskState(val task: Task) {

    abstract fun start()

    abstract fun pause()

    abstract fun resume()

    abstract fun cancel()

    abstract fun delete()

    abstract fun restart()

    open fun onStartState() {}
    open fun onFinishState() {}

    abstract fun getType(): StateType

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

    override fun delete() {

    }

    override fun cancel() {

    }

    override fun restart() {

    }

    override fun getType(): StateType {
        return StateType.INITIAL
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

    override fun delete() {

    }

    override fun cancel() {

    }

    override fun restart() {

    }

    override fun getType(): StateType {
        return StateType.START
    }
}

private const val UPDATE_SPEED = 0x1001

private const val DELAY_TIME = 1000L

private const val STARTING_TASK_STATE_TAG = "StartingTaskState"

class StartingTaskState(var progress: Int, val maxSize: Long, var speed: String, task: Task) : TaskState(task) {

    var currentHandledSize = 0L

    var lastDownloadSize = 0L

    var speedSize: Long = 0L

    private lateinit var speedHandler: SpeedHandler

    override fun onStartState() {
        super.onStartState()

        if (task.startSpeedHandler) {
            speedHandler = SpeedHandler(Looper.getMainLooper(), task)
            speedHandler.sendEmptyMessageDelayed(UPDATE_SPEED, DELAY_TIME)
        }

        task.setTotalSize(FileUtil.formatFileSize(maxSize))

    }

    override fun onFinishState() {
        super.onFinishState()

        if (task.startSpeedHandler) {
            speedHandler.removeMessages(UPDATE_SPEED)
            speedHandler.stopUpdate()
        }

    }

    override fun start() {

    }

    override fun pause() {

        task.doCancelTask()

        Log.d(STARTING_TASK_STATE_TAG, "set state to pause")

        task.setCurrentState(PauseTaskState(progress, maxSize, speed, task))

    }

    override fun resume() {

    }

    override fun cancel() {
        task.doCancelTask()
    }

    override fun delete() {
        task.doDeleteTask()
        task.doCancelTask()
    }

    override fun restart() {

    }

    fun addCurrentHandleFileSize(size: Long) {

        currentHandledSize += size

        calcProgress(currentHandledSize)

        Log.d(STARTING_TASK_STATE_TAG, "addCurrentHandleFileSize size: $size progress: $progress currentHandledSize: $currentHandledSize maxSize: $maxSize")

    }

    fun setCurrentHandleFileSize(size: Long) {

        currentHandledSize = size

        if (currentHandledSize == 0L && maxSize == 0L)
            progress = task.max

        calcProgress(size)

        Log.d(STARTING_TASK_STATE_TAG, "setCurrentHandleFileSize size: $size progress: $progress currentHandledSize: $currentHandledSize maxSize: $maxSize")

    }

    private fun calcProgress(currentHandledSize: Long) {
        val currentProgress = (currentHandledSize * task.max / maxSize).toFloat()

        progress = currentProgress.toInt()
    }

    override fun getType(): StateType {
        return StateType.STARTING
    }

}

class SpeedHandler(looper: Looper, val task: Task) : Handler(looper) {

    private var stopUpdate = false

    fun stopUpdate() {
        stopUpdate = true
    }

    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)

        when (msg?.what) {

            UPDATE_SPEED -> {

                val taskState = task.getCurrentState()

                if (taskState is StartingTaskState && !stopUpdate) {

                    val speedSize = taskState.currentHandledSize - taskState.lastDownloadSize

                    taskState.speedSize = speedSize

                    taskState.speed = FileUtil.formatFileSize(speedSize) + "/s"

                    taskState.lastDownloadSize = taskState.currentHandledSize

                    Log.d("task", "SpeedHandler,set current state: " + taskState.getType())

                    task.setCurrentState(taskState)

                    sendEmptyMessageDelayed(UPDATE_SPEED, DELAY_TIME)

                }

            }

        }

    }

}

class PauseTaskState(var progress: Int, val maxSize: Long, var speed: String, task: Task) : TaskState(task) {

    override fun start() {

        resume()

    }

    override fun pause() {

    }

    override fun resume() {

        val startTaskState = StartTaskState(task)

        task.setCurrentState(startTaskState)

        startTaskState.start()

    }

    override fun delete() {
        task.doDeleteTask()
    }

    override fun cancel() {

    }

    override fun restart() {

    }

    override fun getType(): StateType {
        return StateType.PAUSE
    }

}

private const val FINISH_TASK_TAG = "finish_task_tag"

class FinishTaskState(maxSize: Long, task: Task) : TaskState(task) {

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

    override fun delete() {

    }

    override fun cancel() {

    }

    override fun restart() {
    }

    override fun getType(): StateType {
        return StateType.FINISH
    }

}

class ErrorTaskState(task: Task) : TaskState(task) {
    override fun start() {

    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun delete() {
        task.doDeleteTask()
    }

    override fun cancel() {

    }

    override fun restart() {
        task.setCurrentState(StartTaskState(task))
    }

    override fun getType(): StateType {
        return StateType.ERROR
    }

}




