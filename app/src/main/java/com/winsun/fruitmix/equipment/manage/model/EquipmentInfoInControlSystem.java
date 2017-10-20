package com.winsun.fruitmix.equipment.manage.model;

/**
 * Created by Administrator on 2017/10/17.
 */

public class EquipmentInfoInControlSystem {

    private String equipmentName = "";
    private EquipmentHardware equipmentHardware;
    private EquipmentMemory equipmentMemory;
    private EquipmentCPU equipmentCPU;

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
}
