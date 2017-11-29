package com.winsun.fruitmix.logged.in.user;

import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.equipment.search.data.Equipment;

/**
 * Created by Administrator on 2017/11/29.
 */

public interface LoggedInUserRemoteDataSource {

    void checkFoundedEquipment(Equipment foundedEquipment, LoggedInUser loggedInUser, BaseOperateDataCallback<Boolean> callback);

}
