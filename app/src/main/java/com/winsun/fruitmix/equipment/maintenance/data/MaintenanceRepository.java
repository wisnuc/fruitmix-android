package com.winsun.fruitmix.equipment.maintenance.data;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.thread.manage.ThreadManager;

/**
 * Created by Administrator on 2018/1/2.
 */

public class MaintenanceRepository extends BaseDataRepository implements MaintenanceDataSource {

    private MaintenanceDataSource mMaintenanceDataSource;

    public MaintenanceRepository(ThreadManager threadManager, MaintenanceDataSource maintenanceDataSource) {
        super(threadManager);
        mMaintenanceDataSource = maintenanceDataSource;
    }

    @Override
    public void getDiskState(final String ip,final BaseLoadDataCallback<VolumeState> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mMaintenanceDataSource.getDiskState(ip,createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void startSystem(final String ip,final String volumeUUID, final BaseOperateDataCallback<Void> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mMaintenanceDataSource.startSystem(ip,volumeUUID, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }
}
