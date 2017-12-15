package com.winsun.fruitmix.equipment.search.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/8/28.
 */

public interface EquipmentDataSource {

    int EQUIPMENT_SYSTEM_ERROR = 0x1001;
    int EQUIPMENT_FRUITMIX_ERROR = 0x1002;
    int EQUIPMENT_READY = 0x1003;
    int EQUIPMENT_UNINITIALIZED = 0x1004;
    int EQUIPMENT_FAIL_LAST_LAUNCHER = 0x1005;
    int EQUIPMENT_FAIL_ENOALT = 0x1006;
    int EQUIPMENT_UNKNOWN_ERROR = 0x1007;
    int EQUIPMENT_MAINTENANCE = 0x1008;

    void getUsersInEquipment(Equipment equipment, BaseLoadDataCallback<User> callback);

    void checkEquipmentState(String equipmentIP, BaseOperateDataCallback<Integer> callback);

    void getEquipmentTypeInfo(String equipmentIP, BaseLoadDataCallback<EquipmentTypeInfo> callback);

}
