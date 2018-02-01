package com.winsun.fruitmix.group.setting;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

/**
 * Created by Administrator on 2018/1/31.
 */

public class GroupSettingViewModel {

    public final ObservableField<String> groupName = new ObservableField<>();

    public final ObservableField<String> deviceName = new ObservableField<>();

    public final ObservableBoolean showCheckMoreMembers = new ObservableBoolean();

}
