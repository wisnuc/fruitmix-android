package com.winsun.fruitmix.equipment.search.data;

import android.content.Context;

import com.winsun.fruitmix.R;

/**
 * Created by Administrator on 2017/8/24.
 */

public class EquipmentTypeInfo {

    public static final String WS215I = "ws215i";

    private String type;
    private String label;

    public EquipmentTypeInfo() {

        type = "";
        label = "";

    }

    public String getType(Context context) {

        if (type.isEmpty())
            return context.getString(R.string.virtual_machine);
        else
            return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public String getFormatLabel(Context context) {
        if (label.isEmpty())
            return context.getString(R.string.unknown_equipment_label);
        else
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
