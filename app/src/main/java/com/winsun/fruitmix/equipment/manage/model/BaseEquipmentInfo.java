package com.winsun.fruitmix.equipment.manage.model;

/**
 * Created by Administrator on 2017/10/17.
 */

public class BaseEquipmentInfo {

    private String equipmentName = "";
    private EquipmentHardware equipmentHardware;
    private EquipmentMemory equipmentMemory;
    private EquipmentCPU equipmentCPU;
    private EquipmentStorage equipmentStorage;
    private EquipmentFileSystem equipmentFileSystem;

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public EquipmentHardware getEquipmentHardware() {
        return equipmentHardware;
    }

    public void setEquipmentHardware(EquipmentHardware equipmentHardware) {
        this.equipmentHardware = equipmentHardware;
    }

    public EquipmentMemory getEquipmentMemory() {
        return equipmentMemory;
    }

    public void setEquipmentMemory(EquipmentMemory equipmentMemory) {
        this.equipmentMemory = equipmentMemory;
    }

    public EquipmentCPU getEquipmentCPU() {
        return equipmentCPU;
    }

    public void setEquipmentCPU(EquipmentCPU equipmentCPU) {
        this.equipmentCPU = equipmentCPU;
    }

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
