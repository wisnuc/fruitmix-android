package com.winsun.fruitmix.equipment;

import android.content.Context;

/**
 * Created by Administrator on 2017/7/27.
 */

public class InjectEquipmentManger {

    public static EquipmentSearchManager provideEquipmentSearchManager(Context context) {
        return EquipmentSearchManager.getInstance(context);
    }

}
