package com.winsun.fruitmix.refactor.common;

/**
 * Created by Administrator on 2017/1/24.
 */

public interface BaseView {

    void showNoNetwork();

    void showLoadingUI();

    void dismissLoadingUI();

    void showNoContentUI();

    void showDialog();

    void dismissDialog();

}
