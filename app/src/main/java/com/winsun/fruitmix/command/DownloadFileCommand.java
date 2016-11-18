package com.winsun.fruitmix.command;

import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;

/**
 * Created by Administrator on 2016/11/18.
 */

public class DownloadFileCommand extends AbstractCommand {

    private AbstractRemoteFile abstractRemoteFile;

    public DownloadFileCommand(AbstractRemoteFile abstractRemoteFile) {
        this.abstractRemoteFile = abstractRemoteFile;
    }

    @Override
    public void execute() {
        abstractRemoteFile.downloadFile();
    }

    @Override
    public void unExecute() {

    }
}
