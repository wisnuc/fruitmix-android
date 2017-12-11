package com.winsun.fruitmix.equipment.initial.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

/**
 * Created by Administrator on 2017/12/9.
 */

public class FirstInitialFragmentViewModel {

    public final ObservableField<String> installDiskMode = new ObservableField<>();

    public final ObservableBoolean selectInstallDiskModeEnable = new ObservableBoolean(false);

}
