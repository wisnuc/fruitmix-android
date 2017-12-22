package com.winsun.fruitmix.plugin;

import android.databinding.ObservableBoolean;

/**
 * Created by Administrator on 2017/12/22.
 */

public class PluginViewModel {

    public final ObservableBoolean sambaOpenOrNot = new ObservableBoolean();
    public final ObservableBoolean dlnaOpenOrNot = new ObservableBoolean();
    public final ObservableBoolean btOpenOrNot = new ObservableBoolean();

    public final ObservableBoolean pluginUpdateEnable = new ObservableBoolean(true);

}
