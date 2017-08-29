package com.winsun.fruitmix.model;

/**
 * Created by Administrator on 2017/8/24.
 */

public class EquipmentInfo {

    public static final String WS215I = "ws215i";
    public static final String VIRTUAL_MACHINE = "虚拟机";

    private String type;
    private String label;

    public EquipmentInfo() {

        type = "未知";
        label = "未知";

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "type: " + type + " label: " + label;
    }
}
