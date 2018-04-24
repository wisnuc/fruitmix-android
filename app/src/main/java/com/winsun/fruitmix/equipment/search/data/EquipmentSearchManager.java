package com.winsun.fruitmix.equipment.search.data;

public interface EquipmentSearchManager {

    void startDiscovery(EquipmentFoundedListener equipmentFoundedListener);

    void stopDiscovery();

}
