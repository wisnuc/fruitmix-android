package com.winsun.fruitmix.equipment.manage.data;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.equipment.manage.model.BaseEquipmentInfo;
import com.winsun.fruitmix.equipment.manage.model.EquipmentNetworkInfo;
import com.winsun.fruitmix.equipment.manage.model.EquipmentTimeInfo;
import com.winsun.fruitmix.thread.manage.ThreadManager;

/**
 * Created by Administrator on 2017/10/17.
 */

public class EquipmentInfoRepository extends BaseDataRepository implements EquipmentInfoDataSource {

    private EquipmentInfoDataSource equipmentInfoDataSource;

    public EquipmentInfoRepository(ThreadManager threadManager, EquipmentInfoDataSource equipmentInfoDataSource) {
        super(threadManager);
        this.equipmentInfoDataSource = equipmentInfoDataSource;
    }

    @Override
    public void getBaseEquipmentInfo(final BaseLoadDataCallback<BaseEquipmentInfo> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                equipmentInfoDataSource.getBaseEquipmentInfo(createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getEquipmentNetworkInfo(final BaseLoadDataCallback<EquipmentNetworkInfo> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                equipmentInfoDataSource.getEquipmentNetworkInfo(createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getEquipmentTimeInfo(final BaseLoadDataCallback<EquipmentTimeInfo> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                equipmentInfoDataSource.getEquipmentTimeInfo(createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void shutdownEquipment(final BaseOperateDataCallback<Boolean> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                equipmentInfoDataSource.shutdownEquipment(createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void rebootEquipment(final BaseOperateDataCallback<Boolean> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                equipmentInfoDataSource.rebootEquipment(createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void rebootAndEnterMaintenanceMode(final BaseOperateDataCallback<Boolean> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                equipmentInfoDataSource.rebootAndEnterMaintenanceMode(createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void modifyEquipmentLabel(final String newEquipmentLabel, final BaseOperateDataCallback<Boolean> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                equipmentInfoDataSource.modifyEquipmentLabel(newEquipmentLabel, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }
}
