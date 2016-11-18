package com.winsun.fruitmix.command;

import com.winsun.fruitmix.fileModule.download.FileDownloadManager;

import java.util.List;

/**
 * Created by Administrator on 2016/11/18.
 */

public class DeleteDownloadedFileCommand extends AbstractCommand {

    private List<String> fileUUIDs;

    public DeleteDownloadedFileCommand(List<String> fileUUIDs) {
        this.fileUUIDs = fileUUIDs;
    }

    @Override
    public void execute() {

        FileDownloadManager.INSTANCE.deleteFileDownloadItem(fileUUIDs);
    }

    @Override
    public void unExecute() {

    }
}
