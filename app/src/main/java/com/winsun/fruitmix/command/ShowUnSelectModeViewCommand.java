package com.winsun.fruitmix.command;

import com.winsun.fruitmix.interfaces.OnViewSelectListener;

/**
 * Created by Administrator on 2016/11/18.
 */

public class ShowUnSelectModeViewCommand extends AbstractCommand {
    private OnViewSelectListener onViewSelectListener;

    public ShowUnSelectModeViewCommand(OnViewSelectListener onViewSelectListener) {
        this.onViewSelectListener = onViewSelectListener;
    }

    @Override
    public void execute() {
        onViewSelectListener.unSelectMode();
    }

    @Override
    public void unExecute() {
        onViewSelectListener.selectMode();
    }
}
