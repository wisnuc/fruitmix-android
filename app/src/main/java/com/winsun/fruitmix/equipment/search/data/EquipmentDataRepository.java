package com.winsun.fruitmix.equipment.search.data;

import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/8/28.
 */

public class EquipmentDataRepository extends BaseDataRepository implements EquipmentDataSource {

    public static final String TAG = EquipmentDataRepository.class.getSimpleName();

    private EquipmentRemoteDataSource equipmentRemoteDataSource;

    private EquipmentTypeInfo currentEquipmentTypeInfo;
    private String currentIP;

    private static EquipmentDataRepository instance;

    public static EquipmentDataRepository getInstance(ThreadManager threadManager, EquipmentRemoteDataSource equipmentRemoteDataSource) {

        if (instance == null)
            instance = new EquipmentDataRepository(threadManager, equipmentRemoteDataSource);

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    private EquipmentDataRepository(ThreadManager threadManager, EquipmentRemoteDataSource equipmentRemoteDataSource) {
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
    public void getEquipmentTypeInfo(final String equipmentIP, final BaseLoadDataCallback<EquipmentTypeInfo> callback) {

        final BaseLoadDataCallback<EquipmentTypeInfo> runOnMainThreadCallback = createLoadCallbackRunOnMainThread(callback);

        if (currentIP != null && currentEquipmentTypeInfo != null && currentIP.equals(equipmentIP)) {

            Log.d(TAG, "getEquipmentTypeInfo: return equipment_blue info in memory cache");

            runOnMainThreadCallback.onSucceed(Collections.singletonList(currentEquipmentTypeInfo), new OperationSuccess());

            return;
        }

        Log.d(TAG, "getEquipmentTypeInfo: get equipment_blue info from station");

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                equipmentRemoteDataSource.getEquipmentTypeInfo(equipmentIP, new BaseLoadDataCallback<EquipmentTypeInfo>() {
                    @Override
                    public void onSucceed(List<EquipmentTypeInfo> data, OperationResult operationResult) {

                        currentIP = equipmentIP;

                        currentEquipmentTypeInfo = data.get(0);

                        runOnMainThreadCallback.onSucceed(Collections.singletonList(currentEquipmentTypeInfo), new OperationSuccess());

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        runOnMainThreadCallback.onFail(operationResult);

                    }
                });
            }
        });

    }

}
