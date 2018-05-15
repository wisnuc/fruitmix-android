package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

abstract class TaskState(val task: Task){

    abstract fun start()

    abstract fun pause()

    abstract fun resume()

    abstract fun cancel()

    abstract fun restart()

}

class InitialTaskState(task: Task):TaskState(task){

    init {
        start()
    }

    override fun start() {

        task.setCurrentState(StartTaskState(task))

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

class StartTaskState(task: Task):TaskState(task){

    init {
        start()
    }

    override fun start() {

        //TODO: check task amount is enough

        task.executeTask()

        task.setCurrentState(StartingTaskState("",task))

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

class StartingTaskState(var speed:String,task: Task):TaskState(task){


    override fun start() {

    }

    override fun pause() {

        task.setCurrentState(PauseTaskState(task))

    }

    override fun resume() {
    }

    override fun cancel() {

        task.cancelTask()

    }

    override fun restart() {

    }

}

class PauseTaskState(task: Task):TaskState(task){

    override fun start() {
    }

    override fun pause() {

    }

    override fun resume() {

        task.setCurrentState(StartTaskState(task))

    }

    override fun cancel() {

    }

    override fun restart() {

    }

}

class FinishTaskState(task: Task):TaskState(task){

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

class ErrorTaskState(task: Task):TaskState(task){
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




