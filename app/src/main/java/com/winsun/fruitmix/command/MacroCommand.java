package com.winsun.fruitmix.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/18.
 */

public class MacroCommand extends AbstractCommand {

    private List<AbstractCommand> commands;

    public MacroCommand() {
        commands = new ArrayList<>();
    }

    @Override
    public void execute() {

        for (AbstractCommand command : commands) {
            command.execute();
        }

    }

    @Override
    public void unExecute() {

        for (AbstractCommand command : commands) {
            command.unExecute();
        }
    }

    @Override
    public void addCommand(AbstractCommand command) {
        commands.add(command);
    }

    @Override
    public void removeCommand(AbstractCommand command) {
        commands.remove(command);
    }

}
