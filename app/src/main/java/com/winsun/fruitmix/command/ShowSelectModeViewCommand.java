package com.winsun.fruitmix.command;

import com.winsun.fruitmix.interfaces.OnViewSelectListener;

/**
 * Created by Administrator on 2016/11/18.
 */

public class ShowSelectModeViewCommand extends AbstractCommand {

    private OnViewSelectListener onViewSelectListener;

    public ShowSelectModeViewCommand(OnViewSelectListener onViewSelectListener) {
        this.onViewSelectListener = onViewSelectListener;
    }

    @Override
    public void execute() {
        onViewSelectListener.selectMode();
    }

    @Override
    public void unExecute() {
        onViewSelectListener.unSelectMode();
    }
}
