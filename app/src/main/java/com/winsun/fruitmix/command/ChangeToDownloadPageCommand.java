package com.winsun.fruitmix.command;

/**
 * Created by Administrator on 2016/11/18.
 */

public class ChangeToDownloadPageCommand extends AbstractCommand {

    public interface ChangeToDownloadPageCallback {

        void changeToDownloadPage();

    }

    private ChangeToDownloadPageCallback changeToDownloadPageCallback;

    public ChangeToDownloadPageCommand(ChangeToDownloadPageCallback callback) {
        this.changeToDownloadPageCallback = callback;
    }

    @Override
    public void execute() {
        changeToDownloadPageCallback.changeToDownloadPage();
    }

    @Override
    public void unExecute() {

    }
}
