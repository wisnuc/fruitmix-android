package com.winsun.fruitmix.file.data.download;

import android.util.Log;

import com.winsun.fruitmix.file.data.station.StationFileRepository;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadPendingState extends FileDownloadState {

    public static final String TAG = FileDownloadPendingState.class.getSimpleName();

    private StationFileRepository stationFileRepository;

    private String currentUserUUID;

    public FileDownloadPendingState(FileDownloadItem fileDownloadItem,StationFileRepository stationFileRepository,String currentUserUUID) {
        super(fileDownloadItem);

        this.stationFileRepository = stationFileRepository;

        this.currentUserUUID = currentUserUUID;
    }

    @Override
    public TaskState getDownloadState() {
        return TaskState.PENDING;
    }

    public StationFileRepository getStationFileRepository() {
        return stationFileRepository;
    }

    public String getCurrentUserUUID() {
        return currentUserUUID;
    }

    @Override
    public void startWork() {
        Log.i(TAG, "startWork: it is pending now");
    }

}
