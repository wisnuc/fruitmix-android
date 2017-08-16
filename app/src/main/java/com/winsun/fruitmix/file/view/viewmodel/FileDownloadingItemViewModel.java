package com.winsun.fruitmix.file.view.viewmodel;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;

/**
 * Created by Administrator on 2017/8/16.
 */

public class FileDownloadingItemViewModel {

    public final ObservableInt maxProgress = new ObservableInt(100);

    public final ObservableField<String> fileName = new ObservableField<>();

    public final ObservableInt currentProgress = new ObservableInt();

}
