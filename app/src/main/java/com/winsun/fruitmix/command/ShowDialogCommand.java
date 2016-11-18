package com.winsun.fruitmix.command;

import android.app.Dialog;

/**
 * Created by Administrator on 2016/11/18.
 */

public class ShowDialogCommand extends AbstractCommand {

    private Dialog dialog;

    public ShowDialogCommand(Dialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void execute() {
        dialog.show();
    }

    @Override
    public void unExecute() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }
}
