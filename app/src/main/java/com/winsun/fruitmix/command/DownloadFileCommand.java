package com.winsun.fruitmix.command;

import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.refactor.business.DataRepository;

/**
 * Created by Administrator on 2017/3/3.
 */

public class DownloadFileCommand extends AbstractCommand {

    private DataRepository mRepository;
    private AbstractRemoteFile abstractRemoteFile;

    public DownloadFileCommand(DataRepository mRepository, AbstractRemoteFile abstractRemoteFile) {
        this.mRepository = mRepository;
        this.abstractRemoteFile = abstractRemoteFile;
    }

    @Override
    public void execute() {
        mRepository.downloadFile(abstractRemoteFile);
    }

    @Override
    public void unExecute() {

    }
}
