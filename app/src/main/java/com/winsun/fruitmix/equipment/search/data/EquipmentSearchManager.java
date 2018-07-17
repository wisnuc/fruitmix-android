package com.winsun.fruitmix.equipment.search.data;

import android.content.Context;

public interface EquipmentSearchManager {

    void startDiscovery(Context context, EquipmentFoundedListener equipmentFoundedListener);

    void stopDiscovery();

}
