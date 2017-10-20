package com.winsun.fruitmix.equipment.search.data;

/**
 * Created by Administrator on 2017/8/24.
 */

public class EquipmentTypeInfo {

    public static final String WS215I = "ws215i";
    public static final String VIRTUAL_MACHINE = "虚拟机";

    private String type;
    private String label;

    public EquipmentTypeInfo() {

        type = WS215I;
        label = WS215I;

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
