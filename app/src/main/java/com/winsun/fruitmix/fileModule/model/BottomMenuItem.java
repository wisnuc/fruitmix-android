package com.winsun.fruitmix.fileModule.model;

/**
 * Created by Administrator on 2016/11/11.
 */

public abstract class BottomMenuItem {

    private String text;

    public BottomMenuItem(String text) {
        this.text = text;
    }

    public abstract void handleOnClickEvent();

    public String getText() {
        return text;
    }
}
