package com.winsun.fruitmix.equipment.data;

import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.model.EquipmentInfo;
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

    private EquipmentInfo currentEquipmentInfo;
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
    public void getEquipmentInfo(final String equipmentIP, final BaseLoadDataCallback<EquipmentInfo> callback) {

        final BaseLoadDataCallback<EquipmentInfo> runOnMainThreadCallback = createLoadCallbackRunOnMainThread(callback);

        if (currentIP != null && currentEquipmentInfo != null && currentIP.equals(equipmentIP)) {

            Log.d(TAG, "getEquipmentInfo: return equipment info in memory cache");

            runOnMainThreadCallback.onSucceed(Collections.singletonList(currentEquipmentInfo), new OperationSuccess());

            return;
        }

        Log.d(TAG, "getEquipmentInfo: get equipment info from station");

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                equipmentRemoteDataSource.getEquipmentInfo(equipmentIP, new BaseLoadDataCallback<EquipmentInfo>() {
                    @Override
                    public void onSucceed(List<EquipmentInfo> data, OperationResult operationResult) {

                        currentIP = equipmentIP;

                        currentEquipmentInfo = data.get(0);

                        runOnMainThreadCallback.onSucceed(Collections.singletonList(currentEquipmentInfo), new OperationSuccess());

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
