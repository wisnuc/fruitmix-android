package com.winsun.fruitmix.command;

import com.winsun.fruitmix.fileModule.interfaces.OnFileInteractionListener;

/**
 * Created by Administrator on 2016/11/18.
 */

public class ChangeToDownloadPageCommand extends AbstractCommand {

    private OnFileInteractionListener onFileInteractionListener;

    public ChangeToDownloadPageCommand(OnFileInteractionListener onFileInteractionListener) {
        this.onFileInteractionListener = onFileInteractionListener;
    }

    @Override
    public void execute() {
        onFileInteractionListener.changeFilePageToFileDownloadFragment();
    }

    @Override
    public void unExecute() {

    }
}
