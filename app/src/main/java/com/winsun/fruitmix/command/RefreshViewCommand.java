package com.winsun.fruitmix.command;

import com.winsun.fruitmix.interfaces.OnViewRefreshListener;

/**
 * Created by Administrator on 2016/11/18.
 */

public class RefreshViewCommand extends AbstractCommand{

    private OnViewRefreshListener onViewRefreshListener;

    public RefreshViewCommand(OnViewRefreshListener onViewRefreshListener){
        this.onViewRefreshListener = onViewRefreshListener;
    }

    @Override
    public void execute() {
        onViewRefreshListener.refreshView();
    }

    @Override
    public void unExecute() {

    }
}
