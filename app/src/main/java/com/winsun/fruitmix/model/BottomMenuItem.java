package com.winsun.fruitmix.model;

import android.app.Dialog;

import com.winsun.fruitmix.command.AbstractCommand;

/**
 * Created by Administrator on 2016/11/11.
 */

public class BottomMenuItem {

    private String text;
    private AbstractCommand command;
    private Dialog dialog;

    private boolean disable = false;

    public BottomMenuItem(String text, AbstractCommand command) {
        this.text = text;
        this.command = command;
    }

    public void handleOnClickEvent() {

        if (disable)
            return;

        command.execute();

        dialog.dismiss();
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
