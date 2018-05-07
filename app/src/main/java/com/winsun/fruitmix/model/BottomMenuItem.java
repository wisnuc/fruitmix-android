package com.winsun.fruitmix.model;

import android.app.Dialog;

import com.winsun.fruitmix.command.AbstractCommand;

/**
 * Created by Administrator on 2016/11/11.
 */

public class BottomMenuItem {

    private int iconResID;

    private int rightResID;

    private String text;
    private AbstractCommand command;
    private Dialog dialog;

    private boolean disable = false;

    public BottomMenuItem(int iconResID,String text, AbstractCommand command) {
        this.iconResID = iconResID;
        this.text = text;
        this.command = command;
    }

    public void handleOnClickEvent() {

        if (disable)
            return;

        command.execute();

        dialog.dismiss();
    }

    public int getIconResID() {
        return iconResID;
    }

    public void setRightResID(int rightResID) {
        this.rightResID = rightResID;
    }

    public int getRightResID() {
        return rightResID;
    }

    public String getText() {
        return text;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }
}
