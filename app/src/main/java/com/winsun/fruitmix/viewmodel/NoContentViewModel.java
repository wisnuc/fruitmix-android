package com.winsun.fruitmix.viewmodel;

import android.databinding.ObservableBoolean;

/**
 * Created by Administrator on 2017/6/27.
 */

public class NoContentViewModel {

    private String noContentText;

    private int noContentImgResId;

    public final ObservableBoolean showNoContent = new ObservableBoolean(false);

    public String getNoContentText() {
        return noContentText;
    }

    public void setNoContentText(String noContentText) {
        this.noContentText = noContentText;
    }

    public int getNoContentImgResId() {
        return noContentImgResId;
    }

    public void setNoContentImgResId(int noContentImgResId) {
        this.noContentImgResId = noContentImgResId;
    }

    public final ObservableBoolean showNoContentImg = new ObservableBoolean(true);

}
