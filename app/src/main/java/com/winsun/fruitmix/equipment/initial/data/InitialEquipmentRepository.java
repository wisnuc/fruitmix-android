package com.winsun.fruitmix.equipment.initial.data;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Administrator on 2017/12/7.
 */

public class InitialEquipmentRepository extends BaseDataRepository implements InitialEquipmentDataSource{

    public InitialEquipmentRepository(ThreadManager threadManager) {
        super(threadManager);
    }

    @NotNull
    @Override
    public Void getStorageInfo(@NotNull String ip, @NotNull BaseLoadDataCallback<EquipmentDiskVolume> callback) {
        return null;
    }

}
