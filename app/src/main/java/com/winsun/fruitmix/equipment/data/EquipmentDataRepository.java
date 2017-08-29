package com.winsun.fruitmix.equipment.data;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.model.EquipmentInfo;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/8/28.
 */

public class EquipmentDataRepository extends BaseDataRepository implements EquipmentDataSource {

    private EquipmentRemoteDataSource equipmentRemoteDataSource;

    public EquipmentDataRepository(ThreadManager threadManager, EquipmentRemoteDataSource equipmentRemoteDataSource) {
        super(threadManager);
        this.equipmentRemoteDataSource = equipmentRemoteDataSource;
    }

    @Override
    public void getUsersInEquipment(final Equipment equipment, final BaseLoadDataCallback<User> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                equipmentRemoteDataSource.getUsersInEquipment(equipment, createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getEquipmentInfo(final String equipmentIP, final BaseLoadDataCallback<EquipmentInfo> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                equipmentRemoteDataSource.getEquipmentInfo(equipmentIP, createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

}
