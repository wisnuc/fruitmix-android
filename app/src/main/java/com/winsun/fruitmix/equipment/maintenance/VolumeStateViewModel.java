package com.winsun.fruitmix.equipment.maintenance;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

/**
 * Created by Administrator on 2017/12/29.
 */

public class VolumeStateViewModel {

    public final ObservableField<String> diskTitle = new ObservableField<>();

    public final ObservableField<String> type = new ObservableField<>();

    public final ObservableField<String> mode = new ObservableField<>();

    public final ObservableBoolean showStartBtn = new ObservableBoolean();

    public final ObservableBoolean isMounted = new ObservableBoolean();

    public final ObservableBoolean noMissing = new ObservableBoolean();

    public final ObservableBoolean lastSystem = new ObservableBoolean();

    public final ObservableBoolean fruitmixOK = new ObservableBoolean();

    public final ObservableBoolean usersOK = new ObservableBoolean();


}
