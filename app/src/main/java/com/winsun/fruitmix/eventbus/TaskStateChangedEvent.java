package com.winsun.fruitmix.eventbus;


import com.winsun.fruitmix.file.data.download.TaskState;

/**
 * Created by Administrator on 2016/11/8.
 */

public class TaskStateChangedEvent {

    private TaskState taskState;

    public TaskStateChangedEvent(TaskState taskState) {
        this.taskState = taskState;
    }

    public TaskState getTaskState() {
        return taskState;
    }

}
