package com.winsun.fruitmix.group.data.viewmodel;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;

/**
 * Created by Administrator on 2018/1/24.
 */

public class FileCommentViewModel {

    public final ObservableInt fileResID = new ObservableInt();

    public final ObservableField<String> shareText = new ObservableField<>();

    public final ObservableField<String> shareFileSize = new ObservableField<>();


}
