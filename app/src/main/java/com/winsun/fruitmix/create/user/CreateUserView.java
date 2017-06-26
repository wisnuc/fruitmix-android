package com.winsun.fruitmix.create.user;

/**
 * Created by Administrator on 2017/6/22.
 */

public interface CreateUserView {

    void hideSoftInput();

    void showProgressDialog(String message);

    void dismissDialog();

    void showToast(String text);
}
