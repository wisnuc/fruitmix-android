package com.winsun.fruitmix.equipment.manage.model;

/**
 * Created by Administrator on 2017/10/17.
 */

public class EquipmentMemory {

    private long totalMemorySize;
    private long freeMemorySize;
    private long availableMemorySize;

    public long getTotalMemorySize() {
        return totalMemorySize;
    }

    public void setTotalMemorySize(long totalMemorySize) {
        this.totalMemorySize = totalMemorySize;
    }

    public long getFreeMemorySize() {
        return freeMemorySize;
    }

    public void setFreeMemorySize(long freeMemorySize) {
        this.freeMemorySize = freeMemorySize;
    }

    public long getAvailableMemorySize() {
        return availableMemorySize;
    }

    public void setAvailableMemorySize(long availableMemorySize) {
        this.availableMemorySize = availableMemorySize;
    }

}
