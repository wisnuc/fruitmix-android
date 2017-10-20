package com.winsun.fruitmix.equipment.manage.model;

/**
 * Created by Administrator on 2017/10/17.
 */

public class EquipmentStorage {

    private long totalSize;
    private long userDataSize;
    private long freeSize;

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getUserDataSize() {
        return userDataSize;
    }

    public void setUserDataSize(long userDataSize) {
        this.userDataSize = userDataSize;
    }

    public long getFreeSize() {
        return freeSize;
    }

    public void setFreeSize(long freeSize) {
        this.freeSize = freeSize;
    }
}
