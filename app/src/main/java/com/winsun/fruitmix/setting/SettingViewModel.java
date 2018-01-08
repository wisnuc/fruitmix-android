package com.winsun.fruitmix.setting;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

/**
 * Created by Administrator on 2018/1/8.
 */

public class SettingViewModel {

    public final ObservableBoolean autoUploadOrNot = new ObservableBoolean(false);
    public final ObservableBoolean alreadyUploadMediaCountTextViewVisibility = new ObservableBoolean(false);
    public final ObservableBoolean onlyAutoUploadWhenConnectedWithWifi = new ObservableBoolean(true);
    public final ObservableField<String> alreadyUploadMediaCountText = new ObservableField<>();
    public final ObservableField<String> cacheSizeText = new ObservableField<>();

    public final ObservableBoolean showFirmwareActivity = new ObservableBoolean();

    public final ObservableBoolean askIfNewFirmwareVersionOccur = new ObservableBoolean();

}