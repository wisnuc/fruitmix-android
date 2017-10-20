package com.winsun.fruitmix.equipment.manage.model;

/**
 * Created by Administrator on 2017/10/17.
 */

public class EquipmentCPU {

    private int cpuCoreNumber;
    private String cpuType = "";
    private long cpuCacheSize;

    public int getCpuCoreNumber() {
        return cpuCoreNumber;
    }

    public void setCpuCoreNumber(int cpuCoreNumber) {
        this.cpuCoreNumber = cpuCoreNumber;
    }

    public String getCpuType() {
        return cpuType;
    }

    public void setCpuType(String cpuType) {
        this.cpuType = cpuType;
    }

    public long getCpuCacheSize() {
        return cpuCacheSize;
    }

    public void setCpuCacheSize(long cpuCacheSize) {
        this.cpuCacheSize = cpuCacheSize;
    }
}
