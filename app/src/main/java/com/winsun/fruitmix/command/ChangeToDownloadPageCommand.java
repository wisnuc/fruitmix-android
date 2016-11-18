package com.winsun.fruitmix.command;

import com.winsun.fruitmix.fileModule.interfaces.OnFileFragmentInteractionListener;

/**
 * Created by Administrator on 2016/11/18.
 */

public class ChangeToDownloadPageCommand extends AbstractCommand {

    private OnFileFragmentInteractionListener onFileFragmentInteractionListener;

    public ChangeToDownloadPageCommand(OnFileFragmentInteractionListener onFileFragmentInteractionListener) {
        this.onFileFragmentInteractionListener = onFileFragmentInteractionListener;
    }

    @Override
    public void execute() {
        onFileFragmentInteractionListener.changeFilePageToFileDownloadFragment();
    }

    @Override
    public void unExecute() {

    }
}
