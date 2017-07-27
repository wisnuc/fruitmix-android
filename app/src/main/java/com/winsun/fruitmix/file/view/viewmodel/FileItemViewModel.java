package com.winsun.fruitmix.file.view.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.winsun.fruitmix.R;

/**
 * Created by Administrator on 2017/7/27.
 */

public class FileItemViewModel {

    public final ObservableBoolean selectMode = new ObservableBoolean(false);

    public final ObservableBoolean showFileIcon = new ObservableBoolean(true);

    public final ObservableField<Integer> fileIconBg = new ObservableField<>(R.drawable.round_circle);


}
