package com.winsun.fruitmix.file.view.viewmodel;

import android.databinding.ObservableBoolean;

/**
 * Created by Administrator on 2017/7/27.
 */

public class FileViewModel {

    public final ObservableBoolean showFileRecyclerView = new ObservableBoolean(false);

    public final ObservableBoolean swipeRefreshEnabled = new ObservableBoolean(true);

}
