package com.winsun.fruitmix.equipment.manage.viewmodel;

/**
 * Created by Administrator on 2017/10/16.
 */

public class EquipmentInfoViewModel implements EquipmentInfoItem{

    private int iconResID;
    private String infoKey;
    private String infoValue;

    public EquipmentInfoViewModel() {
        infoKey = "";
        infoValue = "";
    }

    public int getIconResID() {
        return iconResID;
    }

    public void setIconResID(int iconResID) {
        this.iconResID = iconResID;
    }

    public String getInfoKey() {
        return infoKey;
    }

    public void setInfoKey(String infoKey) {
        this.infoKey = infoKey;
    }

    public String getInfoValue() {
        return infoValue;
    }

    public void setInfoValue(String infoValue) {
        this.infoValue = infoValue;
    }

    @Override
    public int getType() {
        return TYPE_VIEW_MODEL;
    }
}
