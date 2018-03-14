package com.winsun.fruitmix.viewmodel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.v4.content.ContextCompat;

import com.winsun.fruitmix.R;

/**
 * Created by Administrator on 2017/6/29.
 */

public class LoadingViewModel {

    public LoadingViewModel(Context context) {

        background.set(R.color.white);

    }

    public LoadingViewModel(Context context, int colorId) {

        background.set(colorId);

    }

    public final ObservableInt background = new ObservableInt();

    public final ObservableBoolean showLoading = new ObservableBoolean(true);

}
