package com.winsun.fruitmix.equipment.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.model.EquipmentInfo;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/8/28.
 */

public interface EquipmentDataSource {

    void getUsersInEquipment(Equipment equipment, BaseLoadDataCallback<User> callback);

    void getEquipmentInfo(String equipmentIP, BaseLoadDataCallback<EquipmentInfo> callback);

}
