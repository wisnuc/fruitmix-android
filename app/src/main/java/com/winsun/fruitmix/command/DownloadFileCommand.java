package com.winsun.fruitmix.command;

import android.util.Log;

import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadManager;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.station.StationFileRepository;

import java.io.File;
import java.util.Collections;

import static com.winsun.fruitmix.util.FileUtil.getDownloadFileStoreFolderPath;

/**
 * Created by Administrator on 2016/11/18.
 */

public class DownloadFileCommand extends AbstractCommand {

    public static final String TAG = DownloadFileCommand.class.getSimpleName();

    private AbstractRemoteFile abstractRemoteFile;

    private FileDownloadItem fileDownloadItem;

    private StationFileRepository stationFileRepository;

    private String currentUserUUID;

    private String driveUUID;

    private FileDownloadManager fileDownloadManager;

    public DownloadFileCommand(FileDownloadManager fileDownloadManager, AbstractRemoteFile abstractRemoteFile, StationFileRepository stationFileRepository, String currentUserUUID, String driveUUID) {
        this.abstractRemoteFile = abstractRemoteFile;
        this.fileDownloadManager = fileDownloadManager;

        this.stationFileRepository = stationFileRepository;
        this.currentUserUUID = currentUserUUID;
        this.driveUUID = driveUUID;

    }

    @Override
    public void execute() {
        fileDownloadItem = new FileDownloadItem(abstractRemoteFile.getName(), Long.parseLong(abstractRemoteFile.getSize()), abstractRemoteFile.getUuid(), abstractRemoteFile.getParentFolderUUID(), driveUUID);

        fileDownloadManager.addFileDownloadItem(fileDownloadItem, stationFileRepository, currentUserUUID);
    }

    @Override
    public void unExecute() {

        fileDownloadItem.cancelDownloadItem();

        fileDownloadManager.deleteFileDownloadItem(Collections.singletonList(fileDownloadItem.getFileUUID()));

        File downloadFile = new File(getDownloadFileStoreFolderPath(), abstractRemoteFile.getName());

        if (downloadFile.exists()) {
            boolean result = downloadFile.delete();

            Log.d(TAG, "cancel download,delete file result: " + result);
        }

    }

    public FileDownloadItem getFileDownloadItem() {
        return fileDownloadItem;
    }
}
