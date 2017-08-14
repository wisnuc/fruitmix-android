package com.winsun.fruitmix.file.data.download;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.executor.DownloadFileTask;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.util.FileUtil;

import java.util.concurrent.Future;

/**
 * Created by Administrator on 2017/6/16.
 */

public class FileStartDownloadState extends FileDownloadState {

    private StationFileRepository stationFileRepository;

    private String currentUserUUID;


    public FileStartDownloadState(FileDownloadItem fileDownloadItem,StationFileRepository stationFileRepository,String currentUserUUID) {
        super(fileDownloadItem);

        this.stationFileRepository = stationFileRepository;

        this.currentUserUUID = currentUserUUID;

    }

    @Override
    public DownloadState getDownloadState() {
        return DownloadState.START_DOWNLOAD;
    }

    @Override
    public void startWork() {

        if (!FileUtil.checkExternalDirectoryForDownloadAvailableSizeEnough()) {
            getFileDownloadItem().setFileDownloadState(new FileDownloadNoEnoughSpaceState(getFileDownloadItem()));
        } else {

            ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;
            DownloadFileTask downloadFileTask = new DownloadFileTask(this, stationFileRepository,currentUserUUID);
            Future<Boolean> future = instance.doOneTaskInDownloadFileThreadPool(downloadFileTask);

            getFileDownloadItem().setFuture(future);
        }

    }
}
