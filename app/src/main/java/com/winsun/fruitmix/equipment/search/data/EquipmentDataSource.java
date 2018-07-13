package com.winsun.fruitmix.equipment.search.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/8/28.
 */

public interface EquipmentDataSource {

    void getUsersInEquipment(Equipment equipment, BaseLoadDataCallback<User> callback);

    void getEquipmentTypeInfo(String equipmentIP, BaseLoadDataCallback<EquipmentTypeInfo> callback);

}
