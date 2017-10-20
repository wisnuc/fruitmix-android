package com.winsun.fruitmix.equipment.manage.model;

/**
 * Created by Administrator on 2017/10/17.
 */

public class EquipmentInfoInStorage {

    private EquipmentStorage equipmentStorage;
    private EquipmentFileSystem equipmentFileSystem;

    public EquipmentStorage getEquipmentStorage() {
        return equipmentStorage;
    }

    public void setEquipmentStorage(EquipmentStorage equipmentStorage) {
        this.equipmentStorage = equipmentStorage;
    }

    public EquipmentFileSystem getEquipmentFileSystem() {
        return equipmentFileSystem;
    }

    public void setEquipmentFileSystem(EquipmentFileSystem equipmentFileSystem) {
        this.equipmentFileSystem = equipmentFileSystem;
    }
}
