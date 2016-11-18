package com.winsun.fruitmix.fileModule.model;

import android.app.Dialog;

import com.winsun.fruitmix.command.AbstractCommand;

/**
 * Created by Administrator on 2016/11/11.
 */

public class BottomMenuItem {

    private String text;
    private AbstractCommand command;
    private Dialog dialog;

    public BottomMenuItem(String text,AbstractCommand command) {
        this.text = text;
        this.command = command;
    }

    public void handleOnClickEvent(){
        command.execute();

        dialog.dismiss();
    }

    public String getText() {
        return text;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }
}
