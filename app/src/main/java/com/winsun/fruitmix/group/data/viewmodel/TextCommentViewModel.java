package com.winsun.fruitmix.group.data.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

/**
 * Created by Administrator on 2017/7/24.
 */

public class TextCommentViewModel {

    public final ObservableField<String> text = new ObservableField<>();

    public final ObservableBoolean isLeftMode = new ObservableBoolean();
}
