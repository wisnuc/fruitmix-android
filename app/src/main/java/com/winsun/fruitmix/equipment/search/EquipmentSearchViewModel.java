package com.winsun.fruitmix.equipment.search;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

/**
 * Created by Administrator on 2017/8/25.
 */

public class EquipmentSearchViewModel {

    public final ObservableBoolean showEquipmentViewPager = new ObservableBoolean();

    public final ObservableBoolean showEquipmentViewPagerIndicator = new ObservableBoolean();

    public final ObservableBoolean showEquipmentUsers = new ObservableBoolean();

    public final ObservableField<String> equipmentState = new ObservableField<>();

    public final ObservableInt equipmentStateIcon = new ObservableInt();

    private String ip;
    private String equipmentName;
    private int equipmentStateCode;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public int getEquipmentStateCode() {
        return equipmentStateCode;
    }

    public void setEquipmentStateCode(int equipmentStateCode) {
        this.equipmentStateCode = equipmentStateCode;
    }
}
