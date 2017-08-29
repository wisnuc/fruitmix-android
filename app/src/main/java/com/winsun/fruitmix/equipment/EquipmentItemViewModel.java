package com.winsun.fruitmix.equipment;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

/**
 * Created by Administrator on 2017/8/15.
 */

public class EquipmentItemViewModel {

    public final ObservableField<String> type = new ObservableField<>();
    public final ObservableField<String> label = new ObservableField<>();
    public final ObservableField<String> ip = new ObservableField<>();

    public final ObservableInt backgroundColorID = new ObservableInt();
    public final ObservableInt cardBackgroundColorID = new ObservableInt();

    public final ObservableInt equipmentIconID = new ObservableInt();

    public final ObservableBoolean showNoEquipment = new ObservableBoolean();

}
