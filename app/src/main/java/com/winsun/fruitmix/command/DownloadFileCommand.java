package com.winsun.fruitmix.command;

import android.util.Log;

import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam;
import com.winsun.fruitmix.file.data.download.param.FileFromBoxDownloadParam;
import com.winsun.fruitmix.file.data.download.param.FileFromStationFolderDownloadParam;
import com.winsun.fruitmix.file.data.model.FileTaskManager;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.FinishedTaskItemWrapper;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.network.NetworkStateManager;

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

    private NetworkStateManager networkStateManager;

    private String currentUserUUID;

    private String driveUUID;

    private String boxUUID;

    private String stationID;

    private String fileHash;

    private String cloudToken;

    private FileTaskManager fileTaskManager;

    private FileDownloadParam mFileDownloadParam;

    public DownloadFileCommand(FileTaskManager fileTaskManager, AbstractRemoteFile abstractRemoteFile,
                               StationFileRepository stationFileRepository, NetworkStateManager networkStateManager,
                               String currentUserUUID, String driveUUID) {
        this.abstractRemoteFile = abstractRemoteFile;
        this.fileTaskManager = fileTaskManager;

        this.stationFileRepository = stationFileRepository;
        this.networkStateManager = networkStateManager;
        this.currentUserUUID = currentUserUUID;
        this.driveUUID = driveUUID;

    }

    public DownloadFileCommand(FileTaskManager fileTaskManager, AbstractRemoteFile abstractRemoteFile,
                               StationFileRepository stationFileRepository, NetworkStateManager networkStateManager,
                               String currentUserUUID, FileDownloadParam fileDownloadParam) {

        this.abstractRemoteFile = abstractRemoteFile;
        this.fileTaskManager = fileTaskManager;

        this.stationFileRepository = stationFileRepository;
        this.networkStateManager = networkStateManager;
        this.currentUserUUID = currentUserUUID;

        mFileDownloadParam = fileDownloadParam;

    }

    public DownloadFileCommand(FileTaskManager fileTaskManager, String fileHash, AbstractRemoteFile abstractRemoteFile,
                               StationFileRepository stationFileRepository, NetworkStateManager networkStateManager,
                               String currentUserUUID, String boxUUID, String stationID, String cloudToken) {
        this.fileHash = fileHash;
        this.abstractRemoteFile = abstractRemoteFile;
        this.fileTaskManager = fileTaskManager;

        this.stationFileRepository = stationFileRepository;
        this.networkStateManager = networkStateManager;
        this.currentUserUUID = currentUserUUID;
        this.boxUUID = boxUUID;
        this.stationID = stationID;

        this.cloudToken = cloudToken;
    }

    @Override
    public void execute() {

//        if (boxUUID != null) {
//
//            FileDownloadParam fileDownloadParam = new FileFromBoxDownloadParam(boxUUID, stationID,fileHash, cloudToken);
//
//            fileDownloadItem = new FileDownloadItem(abstractRemoteFile.getName(), abstractRemoteFile.getSize(), fileHash,
//                    fileDownloadParam);
//
//        } else {
//
//            FileDownloadParam fileDownloadParam = new FileFromStationFolderDownloadParam(abstractRemoteFile.getUuid(),
//                    abstractRemoteFile.getParentFolderUUID(), driveUUID, abstractRemoteFile.getName());
//
//            fileDownloadItem = new FileDownloadItem(abstractRemoteFile.getName(), abstractRemoteFile.getSize(), abstractRemoteFile.getUuid(),
//                    fileDownloadParam);
//
//        }

        if (mFileDownloadParam instanceof FileFromBoxDownloadParam) {

            fileDownloadItem = new FileDownloadItem(abstractRemoteFile.getName(), abstractRemoteFile.getSize(),
                    ((RemoteFile) abstractRemoteFile).getFileHash(), mFileDownloadParam);

        } else {

            fileDownloadItem = new FileDownloadItem(abstractRemoteFile.getName(), abstractRemoteFile.getSize(),
                    abstractRemoteFile.getUuid(), mFileDownloadParam);

        }


        fileTaskManager.addFileDownloadItem(fileDownloadItem, stationFileRepository,
                networkStateManager, currentUserUUID);

    }

    @Override
    public void unExecute() {

        fileDownloadItem.cancelTaskItem();

        fileTaskManager.deleteFileTaskItem(Collections.singletonList(new FinishedTaskItemWrapper(fileDownloadItem.getFileUUID(), abstractRemoteFile.getName())));

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
