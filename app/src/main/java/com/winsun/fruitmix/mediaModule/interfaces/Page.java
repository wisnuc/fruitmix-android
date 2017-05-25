package com.winsun.fruitmix.mediaModule.interfaces;

import android.view.View;

/**
 * Created by Administrator on 2016/11/2.
 */
public interface Page {
    void onDidAppear();

    View getView();

    void refreshView();

}
