package com.winsun.fruitmix.equipment;

import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/8/15.
 */

public interface EquipmentSearchView {

    void handleLoginWithUserSucceed();

    void handleLoginWithUserFail(Equipment equipment, User user);


}
