package com.winsun.fruitmix.file.view.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

/**
 * Created by Administrator on 2017/8/16.
 */

public class FileDownloadedItemViewModel {

    public final ObservableField<String> fileName = new ObservableField<>();
    public final ObservableField<String> fileSize = new ObservableField<>();

    public final ObservableBoolean fileIconVisibility = new ObservableBoolean();

    public final ObservableBoolean fileIconBgVisibility = new ObservableBoolean();
    public final ObservableInt fileIconBgBackgroundSource = new ObservableInt();

}
