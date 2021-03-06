package com.winsun.fruitmix.logged.in.user;

import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.equipment.search.data.Equipment;
import com.winsun.fruitmix.user.User;

import java.util.Collection;

/**
 * Created by Administrator on 2017/7/4.
 */

public interface LoggedInUserDataSource extends LoggedInUserDBSource{

    void checkFoundedEquipment(Equipment foundedEquipment, LoggedInUser loggedInUser, BaseOperateDataCallback<Boolean> callback);

}
