package com.winsun.fruitmix.interfaces;

import android.app.Dialog;
import android.content.Intent;

/**
 * Created by Administrator on 2017/6/22.
 */

public interface BaseView {

    void finishView();

    void setResult(int resultCode);

    void setResult(int resultCode,Intent data);

    Dialog showProgressDialog(String message);

    void dismissDialog();

    void showToast(String text);

    void showCustomErrorCode(String text);

    void onBackPressed();

    String getString(int resID);

    String getString(int resID,Object... formatArgs);

}
