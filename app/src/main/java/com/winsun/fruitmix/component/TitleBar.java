package com.winsun.fruitmix.component;

import android.support.v7.widget.Toolbar;

/**
 * Created by Administrator on 2016/4/19.
 */
public class TitleBar {

    Toolbar toolbar;

    public TitleBar(Toolbar toolbar_, String title) {
        toolbar=toolbar_;
        toolbar.setTitle(title);
    }

    public void setTitle(String title) {
        toolbar.setTitle(title);
    }
}
