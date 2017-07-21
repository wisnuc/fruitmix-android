package com.winsun.fruitmix.interfaces;

import android.app.Dialog;

/**
 * Created by Administrator on 2017/6/22.
 */

public interface BaseView {

    void finishView();

    void setResultCode(int resultCode);

    Dialog showProgressDialog(String message);

    void dismissDialog();

    void showToast(String text);
}
