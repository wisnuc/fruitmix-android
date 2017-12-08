package com.winsun.fruitmix.equipment.search;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

/**
 * Created by Administrator on 2017/8/25.
 */

public class EquipmentSearchViewModel {

    public final ObservableBoolean showEquipmentViewPager = new ObservableBoolean();

    public final ObservableBoolean showEquipmentViewPagerIndicator = new ObservableBoolean();

    public final ObservableBoolean showEquipmentUsers = new ObservableBoolean();

    public final ObservableField<String> equipmentState = new ObservableField<>();

}
