package com.winsun.fruitmix.equipment.maintenance.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;

/**
 * Created by Administrator on 2018/1/2.
 */

public interface MaintenanceDataSource {

    void getDiskState(String ip,BaseLoadDataCallback<VolumeState> callback);

    void startSystem(String ip,String volumeUUID, BaseOperateDataCallback<Void> callback);

}
