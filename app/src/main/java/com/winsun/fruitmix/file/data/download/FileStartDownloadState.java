package com.winsun.fruitmix.file.data.download;

import com.winsun.fruitmix.executor.DownloadFileTask;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.network.NetworkState;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import java.util.concurrent.Future;

/**
 * Created by Administrator on 2017/6/16.
 */

public class FileStartDownloadState extends FileDownloadState {

    private StationFileRepository stationFileRepository;

    private String currentUserUUID;

    private ThreadManager threadManager;

    private NetworkStateManager networkStateManager;

    public FileStartDownloadState(FileDownloadItem fileDownloadItem, StationFileRepository stationFileRepository,
                                  ThreadManager threadManager, String currentUserUUID,
                                  NetworkStateManager networkStateManager) {
        super(fileDownloadItem);

        this.stationFileRepository = stationFileRepository;

        this.currentUserUUID = currentUserUUID;

        this.threadManager = threadManager;

        this.networkStateManager = networkStateManager;
    }

    @Override
    public TaskState getDownloadState() {
        return TaskState.START_DOWNLOAD_OR_UPLOAD;
    }

    @Override
    public void startWork() {

        if (!FileUtil.checkExternalDirectoryForDownloadAvailableSizeEnough()) {
            getFileDownloadItem().setFileDownloadState(new FileDownloadNoEnoughSpaceState(getFileDownloadItem()));
        } else {

            NetworkState networkState = networkStateManager.getNetworkState();

            if (!networkState.isMobileConnected() && !networkState.isWifiConnected()) {
                getFileDownloadItem().setFileDownloadState(new FileDownloadErrorState(getFileDownloadItem()));
                return;
            }

            DownloadFileTask downloadFileTask = new DownloadFileTask(this, stationFileRepository, currentUserUUID);

            Future<Boolean> future = threadManager.runOnDownloadFileThread(downloadFileTask);

            getFileDownloadItem().setFuture(future);
        }

    }
}
