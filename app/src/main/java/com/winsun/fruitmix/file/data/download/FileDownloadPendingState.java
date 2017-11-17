package com.winsun.fruitmix.file.data.download;

import android.util.Log;

import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.network.NetworkStateManager;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadPendingState extends FileDownloadState {

    public static final String TAG = FileDownloadPendingState.class.getSimpleName();

    private StationFileRepository stationFileRepository;

    private String currentUserUUID;

    private NetworkStateManager networkStateManager;

    public FileDownloadPendingState(FileDownloadItem fileDownloadItem,StationFileRepository stationFileRepository,
                                    String currentUserUUID,NetworkStateManager networkStateManager) {
        super(fileDownloadItem);

        this.stationFileRepository = stationFileRepository;

        this.currentUserUUID = currentUserUUID;

        this.networkStateManager = networkStateManager;
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

    public NetworkStateManager getNetworkStateManager() {
        return networkStateManager;
    }

    @Override
    public void startWork() {
        Log.i(TAG, "startWork: it is pending now");
    }

}
