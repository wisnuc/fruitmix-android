package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

abstract class TaskState(val task: Task) {

    abstract fun start()

    abstract fun pause()

    abstract fun resume()

    abstract fun cancel()

    abstract fun restart()

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

}

class StartTaskState(task: Task) : TaskState(task) {

    override fun start() {

        //TODO: check task amount is enough

        task.executeTask()

        task.setCurrentState(StartingTaskState(10, "51.3KB/s", task))

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun cancel() {

    }

    override fun restart() {

    }

}

class StartingTaskState(var progress: Int, var speed: String, task: Task) : TaskState(task) {

    override fun start() {

    }

    override fun pause() {

        task.setCurrentState(PauseTaskState(progress, speed, task))

    }

    override fun resume() {
    }

    override fun cancel() {

        task.cancelTask()

    }

    override fun restart() {

    }

    fun setCurrentDownloadFileSize(currentDownloadedSize: Long) {

        if (currentDownloadedSize == 0L && task.abstractFile.size == 0L)
            progress = task.max

        val currentProgress = (currentDownloadedSize * task.max / task.abstractFile.size).toFloat()

        progress = currentProgress.toInt()

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

}

class FinishTaskState(task: Task) : TaskState(task) {

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

}




