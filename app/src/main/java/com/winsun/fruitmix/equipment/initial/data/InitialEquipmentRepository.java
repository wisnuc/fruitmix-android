package com.winsun.fruitmix.equipment.initial.data;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Administrator on 2017/12/7.
 */

public class InitialEquipmentRepository extends BaseDataRepository implements InitialEquipmentDataSource {

    private InitialEquipmentRemoteDataSource mInitialEquipmentRemoteDataSource;

    public InitialEquipmentRepository(ThreadManager threadManager, InitialEquipmentRemoteDataSource initialEquipmentRemoteDataSource) {
        super(threadManager);
        mInitialEquipmentRemoteDataSource = initialEquipmentRemoteDataSource;
    }

    @Override
    public void getStorageInfo(@NotNull final String ip, @NotNull final BaseLoadDataCallback<EquipmentDiskVolume> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mInitialEquipmentRemoteDataSource.getStorageInfo(ip, createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

}
