package com.winsun.fruitmix.equipment.manage.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.equipment.manage.model.BaseEquipmentInfo;
import com.winsun.fruitmix.equipment.manage.model.EquipmentNetworkInfo;
import com.winsun.fruitmix.equipment.manage.model.EquipmentTimeInfo;

/**
 * Created by Administrator on 2017/10/17.
 */

public interface EquipmentInfoDataSource {

    void getBaseEquipmentInfo(BaseLoadDataCallback<BaseEquipmentInfo> callback);

    void getEquipmentNetworkInfo(BaseLoadDataCallback<EquipmentNetworkInfo> callback);

    void getEquipmentTimeInfo(BaseLoadDataCallback<EquipmentTimeInfo> callback);

    void shutdownEquipment(BaseOperateDataCallback<Boolean> callback);

    void rebootEquipment(BaseOperateDataCallback<Boolean> callback);

    void rebootAndEnterMaintenanceMode(BaseOperateDataCallback<Boolean> callback);

    void modifyEquipmentLabel(String newEquipmentLabel,BaseOperateDataCallback<Boolean> callback);

}
