package com.winsun.fruitmix.command;

import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadManager;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.station.StationFileRepository;

import java.util.Collections;

/**
 * Created by Administrator on 2016/11/18.
 */

public class DownloadFileCommand extends AbstractCommand {

    private AbstractRemoteFile abstractRemoteFile;

    private FileDownloadItem fileDownloadItem;

    private StationFileRepository stationFileRepository;

    private String currentUserUUID;

    public DownloadFileCommand(AbstractRemoteFile abstractRemoteFile,StationFileRepository stationFileRepository,String currentUserUUID) {
        this.abstractRemoteFile = abstractRemoteFile;

        this.stationFileRepository =stationFileRepository;
        this.currentUserUUID = currentUserUUID;

    }

    @Override
    public void execute() {
        fileDownloadItem = new FileDownloadItem(abstractRemoteFile.getName(), Long.parseLong(abstractRemoteFile.getSize()), abstractRemoteFile.getUuid(),abstractRemoteFile.getParentFolderUUID());

        FileDownloadManager.getInstance().addFileDownloadItem(fileDownloadItem,stationFileRepository,currentUserUUID);
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
