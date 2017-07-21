package com.winsun.fruitmix.command;

import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;

import java.util.Collections;

/**
 * Created by Administrator on 2016/11/18.
 */

public class DownloadFileCommand extends AbstractCommand {

    private AbstractRemoteFile abstractRemoteFile;

    private FileDownloadItem fileDownloadItem;

    public DownloadFileCommand(AbstractRemoteFile abstractRemoteFile) {
        this.abstractRemoteFile = abstractRemoteFile;
    }

    @Override
    public void execute() {
        fileDownloadItem = new FileDownloadItem(abstractRemoteFile.getName(), Long.parseLong(abstractRemoteFile.getSize()), abstractRemoteFile.getUuid(),abstractRemoteFile.getParentFolderUUID());

        FileDownloadManager.getInstance().addFileDownloadItem(fileDownloadItem);
    }

    @Override
    public void unExecute() {

        fileDownloadItem.cancelDownloadItem();

        FileDownloadManager.getInstance().deleteFileDownloadItem(Collections.singletonList(fileDownloadItem.getFileUUID()));

    }

    public FileDownloadItem getFileDownloadItem() {
        return fileDownloadItem;
    }
}
