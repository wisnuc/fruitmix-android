package com.winsun.fruitmix.equipment.search;

import com.winsun.fruitmix.equipment.search.data.Equipment;
import com.winsun.fruitmix.equipment.search.data.EquipmentSearchManager;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/8/15.
 */

public interface EquipmentSearchView {

    void handleLoginWithUserSucceed(boolean autoUpload);

    void handleLoginWithUserFail(Equipment equipment, User user);

    void setBackgroundColor(int color);

    int getCurrentViewPagerItem();

    EquipmentSearchManager getEquipmentSearchManager();

}
