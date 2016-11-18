package com.winsun.fruitmix.command;

/**
 * Created by Administrator on 2016/11/18.
 */

public abstract class AbstractCommand {

    public abstract void execute();

    public abstract void unExecute();

    public void addCommand(AbstractCommand command) {
    }

    public void removeCommand(AbstractCommand command) {
    }
}
