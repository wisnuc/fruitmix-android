package com.winsun.fruitmix.file.data.download;

import com.winsun.fruitmix.executor.DownloadFileTask;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.util.FileUtil;

import java.util.concurrent.Future;

/**
 * Created by Administrator on 2017/6/16.
 */

public class FileStartDownloadState extends FileDownloadState {

    private StationFileRepository stationFileRepository;

    private String currentUserUUID;

    private ThreadManager threadManager;

    public FileStartDownloadState(FileDownloadItem fileDownloadItem, StationFileRepository stationFileRepository, ThreadManager threadManager, String currentUserUUID) {
        super(fileDownloadItem);

        this.stationFileRepository = stationFileRepository;

        this.currentUserUUID = currentUserUUID;

        this.threadManager = threadManager;

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

            DownloadFileTask downloadFileTask = new DownloadFileTask(this, stationFileRepository, currentUserUUID);

            Future<Boolean> future = threadManager.runOnDownloadFileThread(downloadFileTask);

            getFileDownloadItem().setFuture(future);
        }

    }
}
